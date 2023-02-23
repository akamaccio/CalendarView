package it.matmacci.calendarview

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import it.matmacci.calendarview.databinding.ActivityMainBinding
import net.akamaccio.widget.Extensions.Companion.onRange
import net.akamaccio.widget.MmCalendarDayDecorator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding : ActivityMainBinding

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    private var calendarDayBgSelected : Drawable? = null
    private var calendarDayTxtSelected : Int = 0

    private val selectedDaysDecorator = object : MmCalendarDayDecorator{
        private val selectedDates = mutableListOf<LocalDate>()

        fun onNewValue(localDate: LocalDate){
            when(selectedDates.size){
                0 -> {
                    selectedDates.add(localDate)
                    mainBinding.calendarView.notifyCalendarItemsChanged(localDate)
                }
                1 -> { //start date available
                    if(selectedDates.contains(localDate)){
                        //remove from list
                        selectedDates.remove(localDate)
                        //notify date change
                        mainBinding.calendarView.notifyCalendarItemsChanged(localDate)
                    }
                    else { //the selected one is the end date
                        val firstDate = selectedDates.removeFirst()
                        localDate.onRange(firstDate){ date ->
                            selectedDates.add(date)
                            mainBinding.calendarView.notifyCalendarItemsChanged(date)
                            date.plusDays(1)
                        }
                    }
                }
                else -> { //start and end dates available -> clear selection and add new start
                    val dates = arrayOf(localDate, *selectedDates.toTypedArray())
                    selectedDates.clear()
                    selectedDates.add(localDate)
                    mainBinding.calendarView.notifyCalendarItemsChanged(*dates)
                }
            }
        }

        override fun shouldDecorate(date: LocalDate): Boolean {
            return selectedDates.contains(date)
        }

        override fun decorate(view: TextView) {
            view.apply {
                background = calendarDayBgSelected
                setTextColor(calendarDayTxtSelected)
            }
        }
    }

    private val daysToMarkDecorator = object : MmCalendarDayDecorator{
        private val datesToMark = mutableListOf<LocalDate>()

        fun addDates(stringDates : Array<String>){
            datesToMark.addAll(stringDates.map { stringDate -> LocalDate.parse(stringDate, dateFormatter) })
            mainBinding.calendarView.notifyCalendarItemsChanged(*datesToMark.toTypedArray())
        }

        override fun shouldDecorate(date: LocalDate): Boolean {
            return datesToMark.contains(date)
        }

        override fun decorate(view: TextView) {
            view.apply {
                val spannableString = SpannableString(text)
                spannableString.setSpan(DotSpan(5f, Color.RED), 0,
                    spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text = spannableString
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        calendarDayTxtSelected = ContextCompat.getColor(this, R.color.calendar_day_text_selected)
        calendarDayBgSelected = ContextCompat.getDrawable(this, R.drawable.calendar_day_bg_selected)

        mainBinding.calendarView.apply {
            if(savedInstanceState == null){ // prevent configuration if the saved state is available
                minDate = LocalDate.of(2018, 1, 1)
                maxDate = LocalDate.now()
                showOtherDates = true
                firstDayOfWeek = DayOfWeek.SATURDAY
            }
            //add decorators
            decorators.add(object : MmCalendarDayDecorator{
                override fun shouldDecorate(date: LocalDate): Boolean {
                    return !mainBinding.calendarView.isInRange(date)
                }
                override fun decorate(view: TextView) {
                    view.apply {
                        isEnabled = false
                        alpha = 0.1f
                    }
                }
            })
            decorators.add(daysToMarkDecorator)
            decorators.add(selectedDaysDecorator)
            //add listeners
            onDateClickListener = { _, localDate ->
                selectedDaysDecorator.onNewValue(localDate)
            }
            build()
        }

        /* E.g. list from API */
        val today = LocalDate.now()
        daysToMarkDecorator.addDates(arrayOf(
            today.format(dateFormatter),
            today.minusDays(3).format(dateFormatter),
            today.minusDays(5).format(dateFormatter),
            today.minusDays(7).format(dateFormatter),
            today.minusDays(13).format(dateFormatter),
            today.minusDays(24).format(dateFormatter),
            today.minusDays(25).format(dateFormatter),
            today.minusDays(26).format(dateFormatter),
            today.minusDays(44).format(dateFormatter),
            today.minusDays(46).format(dateFormatter),
            today.minusDays(59).format(dateFormatter),
        ))
    }
}
