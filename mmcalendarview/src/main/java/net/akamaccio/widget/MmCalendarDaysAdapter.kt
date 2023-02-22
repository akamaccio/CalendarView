package net.akamaccio.widget

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import net.akamaccio.widget.MmCalendarView.Companion.MAX_SPOTS_IN_CALENDAR
import net.akamaccio.widget.mmcalendarview.R
import java.time.LocalDate
import java.time.YearMonth

/**
 * Created by Matteo Maccioni on 15/02/2023.
 *
 * Adapter for the calendar days of a single month.
 */
internal class MmCalendarDaysAdapter(private val calendarView : MmCalendarView,
                                     private val calendarMonth: YearMonth,
                                     private val monthDays: Array<LocalDate?> = arrayOfNulls(MAX_SPOTS_IN_CALENDAR)
)
    : RecyclerView.Adapter<MmCalendarDaysAdapter.CalendarDayViewHolder>() {

    init {
        val firstDayOfMonth = calendarMonth.atDay(1)
        val firstDayOfMonthIndex = calendarView.daysOfWeek.indexOf(firstDayOfMonth.dayOfWeek)
        if(calendarView.showOtherDates){
            val firstDay = firstDayOfMonth.minusDays(firstDayOfMonthIndex.toLong())
            monthDays.forEachIndexed{ index, _ ->
                monthDays[index] = firstDay.plusDays(index.toLong())
            }
        }
        else{
            for (index in 0 until calendarMonth.lengthOfMonth()){
                monthDays[index+firstDayOfMonthIndex] = firstDayOfMonth.plusDays(index.toLong())
            }
        }
    }

    inner class CalendarDayViewHolder(view : View) : ViewHolder(view){
        internal val calendarDay = view.findViewById<TextView>(R.id.mmcv_calendar_day)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        return CalendarDayViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.mmcv_calendar_day, parent, false)
        )
    }

    override fun getItemCount() = monthDays.size

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val day = monthDays[position]
        day?.let {
            holder.calendarDay?.apply {
                background = null //reset background
                setTextColor(calendarView.dayTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarView.dayTextSize)
                alpha = if(inMonth(day)) 1f else 0.3f
                text = calendarView.calendarDayFormatter.invoke(day)
                //apply decorations
                calendarView.decorators.forEach { decorator ->
                    if(decorator.shouldDecorate(day))
                        decorator.decorate(this)
                }
                //set listeners
                setOnClickListener{
                    calendarView.onDateClickListener?.let { onClick ->
                        onClick(calendarView, day)
                    }
                }
                setOnLongClickListener {
                    calendarView.onDateLongClickListener?.let { onLongClick ->
                        onLongClick(calendarView, day)
                    } ?: false
                }
            }
        }
    }

    private fun inMonth(localDate: LocalDate) = localDate.monthValue == calendarMonth.monthValue

    internal fun notifyLocalDateChanged(localDate: LocalDate){
        val index = monthDays.indexOf(localDate)
        if(index > -1) notifyItemChanged(index)
    }
}