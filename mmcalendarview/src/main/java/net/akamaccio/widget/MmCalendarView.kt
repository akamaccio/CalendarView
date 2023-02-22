package net.akamaccio.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import net.akamaccio.widget.Extensions.Companion.getEnum
import net.akamaccio.widget.mmcalendarview.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.time.temporal.Temporal
import java.time.temporal.WeekFields
import java.util.*

/**
 * Created by Matteo Maccioni on 15/02/2023.
 *
 * Custom layout of a calendar view composed by:
 * <ul>
 *     <li>A top bar with left and right arrows and year/month label.</li>
 *     <li>A mid bar with all week days.</li>
 *     <li>A view pager with a page per month. Every page has a recycler view owning the month days.</li>
 * </ul>
 */
class MmCalendarView(
    context: Context,
    attrs: AttributeSet?,
) : ConstraintLayout(context, attrs) {

    internal class MmcvSavedState : BaseSavedState {
        var firstDayOfWeek : DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var minDate: LocalDate = LocalDate.now().withDayOfMonth(1)
        var maxDate: LocalDate = LocalDate.now()
        var currentMonth: YearMonth = YearMonth.now()
        var showOtherDates : Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source){
            firstDayOfWeek = DayOfWeek.of(source.readInt())
            minDate = LocalDate.ofEpochDay(source.readLong())
            maxDate = LocalDate.ofEpochDay(source.readLong())
            currentMonth = YearMonth.parse(source.readString())
            showOtherDates = source.readString().toBoolean()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(firstDayOfWeek.value)
            out.writeLong(minDate.toEpochDay())
            out.writeLong(maxDate.toEpochDay())
            out.writeString(currentMonth.toString())
            out.writeString(showOtherDates.toString())
        }

        @Suppress("unused")
        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<MmcvSavedState> {
                override fun createFromParcel(source: Parcel) = MmcvSavedState(source)
                override fun newArray(size: Int): Array<MmcvSavedState?> = arrayOfNulls(size)
            }
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchThawSelfOnly(container)
    }

    /*** Constants ***/
    companion object{
        internal const val MAX_SPOTS_IN_CALENDAR = 42 //7 columns (days of week) 6 rows 7 columns (days of week) 6 rows
        internal const val TAG = "Mmcv"

        private const val DEFAULT_MONTH_YEAR_TEXT_SIZE = 24.0f
        private const val DEFAULT_WEEK_DAYS_TEXT_SIZE = 16.0f
        private const val DEFAULT_DAY_TEXT_SIZE = 18.0f
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
    }

    /*** Decorators ***/
    val decorators = mutableListOf<MmCalendarDayDecorator>()
    internal var dayTextSize = sp2px(DEFAULT_DAY_TEXT_SIZE)
    internal var dayTextColor = DEFAULT_TEXT_COLOR
    private var weekDaysTextSize = sp2px(DEFAULT_WEEK_DAYS_TEXT_SIZE)
    private var weekDaysTextColor = DEFAULT_TEXT_COLOR
    private val scaledDensity : Float = getContext().resources.displayMetrics.scaledDensity

    /*** Views ***/
    private val monthOfYear: TextView
    private var arrowLeft: ImageView
    private var arrowRight: ImageView
    private var viewPager: ViewPager2
    private var calendarDaysOfWeekContainer: ViewGroup

    /*** Content ***/
    private val calendarMonths = mutableListOf<YearMonth>()
    var currentCalendarMonth: YearMonth = YearMonth.now()
        private set
    internal lateinit var daysOfWeek : List<DayOfWeek>
    var maxDate: LocalDate = LocalDate.now()
    var minDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    var firstDayOfWeek : DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var showOtherDates = false

    /*** Formatters ***/
    var calendarMonthFormatter : ((YearMonth) -> CharSequence) = { calendarMonth -> "${Month.of(calendarMonth.monthValue).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendarMonth.year}" }
    var calendarDayFormatter : ((LocalDate) -> CharSequence) = { calendarDay -> "${calendarDay.dayOfMonth}"}
    var calendarDayOfWeekFormatter : ((DayOfWeek) -> CharSequence) = {calendarDayOfWeek -> calendarDayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}

    /*** Adapter ***/
    private lateinit var adapter : MmCalendarMonthsAdapter

    /*** Listeners ***/
    var onDateClickListener : ((MmCalendarView, LocalDate) -> Unit)? = null
    var onDateLongClickListener : ((MmCalendarView, LocalDate) -> Boolean)? = null
    var onMonthClickListener : ((MmCalendarView, YearMonth) -> Unit)? = null
    var onMonthChangedListener : ((MmCalendarView, YearMonth) -> Unit)? = null

    init {
        /* init views */
        inflate(getContext(), R.layout.mmcv_calendar_view, this)
        monthOfYear = findViewById(R.id.mmcv_month_of_year)
        arrowLeft = findViewById(R.id.mmcv_arrow_left)
        arrowRight = findViewById(R.id.mmcv_arrow_right)
        calendarDaysOfWeekContainer = findViewById(R.id.mmcv_calendar_days_of_week_container)
        viewPager = findViewById(R.id.mmcv_view_pager)
        /* add listeners */
        arrowLeft.setOnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
        }
        arrowRight.setOnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
        monthOfYear.setOnClickListener {
            onMonthClickListener?.let { onMonthYearClicked ->
                onMonthYearClicked(this, currentCalendarMonth)
            }
        }
    }

    init {
        //attrs management
        attrs?.let {attributeSet ->
            val typedArray = context.theme.obtainStyledAttributes(attributeSet, R.styleable.MmCalendarView, 0, 0)
            try {
                arrowLeft.setImageDrawable(typedArray.getDrawable(R.styleable.MmCalendarView_mmcv_arrow_left))
                arrowRight.setImageDrawable(typedArray.getDrawable(R.styleable.MmCalendarView_mmcv_arrow_right))
                showOtherDates = typedArray.getBoolean(R.styleable.MmCalendarView_mmcv_show_other_dates, false)
                firstDayOfWeek = typedArray.getEnum(R.styleable.MmCalendarView_mmcv_first_day_of_week, WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                weekDaysTextSize = typedArray.getDimension(R.styleable.MmCalendarView_mmcv_text_week_days_size, sp2px(DEFAULT_WEEK_DAYS_TEXT_SIZE))
                weekDaysTextColor = typedArray.getColor(R.styleable.MmCalendarView_mmcv_text_header_color, DEFAULT_TEXT_COLOR)
                monthOfYear.setTextColor(typedArray.getColor(R.styleable.MmCalendarView_mmcv_text_header_color, DEFAULT_TEXT_COLOR))
                monthOfYear.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    typedArray.getDimension(R.styleable.MmCalendarView_mmcv_text_month_size, sp2px(DEFAULT_MONTH_YEAR_TEXT_SIZE))
                )
                dayTextSize = typedArray.getDimension(R.styleable.MmCalendarView_mmcv_text_day_size, sp2px(DEFAULT_DAY_TEXT_SIZE))
                dayTextColor = typedArray.getColor(R.styleable.MmCalendarView_mmcv_text_day_color, DEFAULT_TEXT_COLOR)
                typedArray.getString(R.styleable.MmCalendarView_mmcv_min_date)?.let {date->
                    minDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                }
                typedArray.getString(R.styleable.MmCalendarView_mmcv_max_date)?.let {date->
                    maxDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                }
            }
            catch (dte : DateTimeParseException){
                Log.e(TAG, "Unable to parse the date. Apply the date pattern 'yyyyMMdd' or set programmatically. Default min and max dates values has been applied.\n$dte")
            }
            catch (ex : Exception){
                Log.e(TAG, "An error has occurred on styled attributes management. Default values will be applied. ${ex.message}\n${ex}")
                initDefault()
            }
            finally {
                typedArray.recycle()
            }
        } ?: {
            Log.w(TAG, "AttributeSet is null. Default values will be applied.")
            initDefault()
        }
        //build
        build()
    }

    private fun initDefault(){
        monthOfYear.setTextColor(DEFAULT_TEXT_COLOR)
        monthOfYear.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp2px(DEFAULT_MONTH_YEAR_TEXT_SIZE))
    }

    fun build(){
        daysOfWeek = DayOfWeek.values().asList()
        Collections.rotate(daysOfWeek, -firstDayOfWeek.ordinal) //shift the list of days of week
        for(i in 0 until calendarDaysOfWeekContainer.childCount){
            val textView = calendarDaysOfWeekContainer.getChildAt(i) as? TextView
            textView?.apply {
                setTextColor(weekDaysTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, weekDaysTextSize)
                text = calendarDayOfWeekFormatter.invoke(daysOfWeek[i])
            }
        }
        val firstMonth = YearMonth.from(minDate)
        val lastMonth = YearMonth.from(maxDate)
        //fill year months list
        calendarMonths.clear()
        var month = firstMonth
        while (month.isBefore(lastMonth).or(month == lastMonth)){
            calendarMonths.add(month)
            month = month.plusMonths(1)
        }
        //init view pager and adapter
        adapter = MmCalendarMonthsAdapter(this, calendarMonths)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentCalendarMonth = calendarMonths[position]
                arrowLeft.isEnabled = currentCalendarMonth != firstMonth
                arrowRight.isEnabled = currentCalendarMonth != lastMonth
                monthOfYear.text = calendarMonthFormatter.invoke(currentCalendarMonth)
                onMonthChangedListener?.let { onMonthChanged ->
                    onMonthChanged(this@MmCalendarView, currentCalendarMonth)
                }
            }
        })
        //prevent smooth scrolling -> scroll to last month
        moveTo(currentCalendarMonth)
    }

    override fun onSaveInstanceState(): Parcelable {
        Log.d(TAG, "onSaveInstanceState")
        val ss = MmcvSavedState(super.onSaveInstanceState())
        ss.firstDayOfWeek = firstDayOfWeek
        ss.minDate = minDate
        ss.maxDate = maxDate
        ss.currentMonth = currentCalendarMonth
        ss.showOtherDates = showOtherDates
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.d(TAG, "onRestoreInstanceState")
        when(state){
            is MmcvSavedState -> {
                super.onRestoreInstanceState(state)
                firstDayOfWeek = state.firstDayOfWeek
                minDate = state.minDate
                maxDate = state.maxDate
                currentCalendarMonth = state.currentMonth
                showOtherDates = state.showOtherDates
                build()
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    /**
     * This method scrolls the calendar to the provided [Temporal].
     * @param temporal The temporal reference.
     * @param smoothly TRUE if the scroll must be smooth, FALSE (default) otherwise.
     */
    fun moveTo(temporal : Temporal, smoothly : Boolean = false){
        adapter.indexOf(temporal)?.let { position ->
            if (position > -1) viewPager.setCurrentItem(position, smoothly)
            else Log.w(TAG, "Unable to scroll to $temporal" )
        } ?: Log.w(TAG, "Unable to scroll to $temporal" )
    }

    /**
     * This method refreshed all calendar.
     * This method has a high computing cost and must be used just in case many calendar items has been updated.
     * Use [notifyCalendarItemsChanged] instead.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyCalendarChanged() = adapter.notifyDataSetChanged()

    /**
     * This method updated the provided [Temporal] items (e.g. [LocalDate] and [YearMonth]).
     * @param items The list of temporal items to update.
     */
    fun <T : Temporal> notifyCalendarItemsChanged(vararg items : T){
        adapter.notifyCalendarItemsChanged(*items)
    }

    /**
     * This method checks if the provided [LocalDate] is withing the min and max dates.
     * @param localDate The local date to check.
     * @return TRUE if the provided [LocalDate] is withing the min and max dates, FALSE otherwise.
     */
    fun isInRange(localDate: LocalDate) : Boolean {
        return localDate == minDate || localDate == maxDate
                || (localDate.isAfter(minDate) && localDate.isBefore(maxDate))
    }

    /**
     * This method converts an sp value to pixels value.
     * @param sp The sp value to convert.
     * @return The pixels value
     */
    private fun sp2px(sp: Float) = sp * scaledDensity
}