package net.akamaccio.widget

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import net.akamaccio.widget.MmCalendarView.Companion.TAG
import net.akamaccio.widget.mmcalendarview.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.Temporal

/**
 * Created by Matteo Maccioni on 16/02/2023.
 *
 * Adapter for calendar months.
 */
internal class MmCalendarMonthsAdapter(private val calendarView: MmCalendarView,
                                       private val calendarMonths : List<YearMonth>
) : RecyclerView.Adapter<MmCalendarMonthsAdapter.CalendarMonthViewHolder>() {

    private val calendarMonthAdapterMap : Map<YearMonth, MmCalendarDaysAdapter> =
        calendarMonths.associateWith { calendarMonth ->
            MmCalendarDaysAdapter(calendarView, calendarMonth)
        }

    inner class CalendarMonthViewHolder(view : View) : ViewHolder(view){
        internal val recyclerView = view.findViewById<RecyclerView>(R.id.mmcv_calendar_months_rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarMonthViewHolder {
        return CalendarMonthViewHolder(
            LayoutInflater.from(parent.context).inflate(
            R.layout.mmcv_calendar_months, parent, false)
        )
    }

    override fun getItemCount() = calendarMonths.size

    override fun onBindViewHolder(holder: CalendarMonthViewHolder, position: Int) {
        val calendarMonth = calendarMonths[position]
        holder.recyclerView.apply {
            adapter = calendarMonthAdapterMap[calendarMonth]
            layoutManager = GridLayoutManager(context, 7)
        }
    }

    internal fun indexOf(temporal: Temporal) : Int? {
        return when(temporal){
            is YearMonth ->
                calendarMonthAdapterMap.keys.indexOfFirst { key -> key == temporal }
            is LocalDate ->
                calendarMonthAdapterMap.keys.indexOfFirst { key -> key == YearMonth.from(temporal) }
            else -> {
                Log.w(TAG, "Unhandled calendar element type ${temporal.javaClass}")
                null
            }
        }
    }

    internal fun <T : Temporal> notifyCalendarItemsChanged(vararg items : T){
        if(items.isNotEmpty()){
            items.forEach { temporal ->
                when(temporal){
                    is YearMonth -> {
                        val index: Int = calendarMonthAdapterMap.keys.indexOfFirst { key -> key == temporal }
                        if (index > -1) notifyItemChanged(index)
                    }
                    is LocalDate -> {
                        val calendarMonth = YearMonth.from(temporal)
                        val calendarMonthsRange = arrayOf(calendarMonth.minusMonths(1),
                            calendarMonth,
                            calendarMonth.plusMonths(1)
                        )
                        calendarMonthAdapterMap.filter { entry ->
                            calendarMonthsRange.contains(entry.key)
                        }.forEach {entry ->
                            entry.value.notifyLocalDateChanged(temporal)
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unhandled calendar element type ${temporal.javaClass}")
                    }
                }
            }
        }
    }
}