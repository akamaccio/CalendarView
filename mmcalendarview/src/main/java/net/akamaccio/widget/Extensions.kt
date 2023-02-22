package net.akamaccio.widget

import android.content.res.TypedArray
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

/**
 * Created by Matteo Maccioni on 21/02/2023.
 *
 * Kotlin extensions.
 */
class Extensions {
    companion object{
        inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
            getInt(index, -1).let { if (it >= 0) enumValues<T>()[it] else default }

        /**
         * This function returns an unmodifiable list of months between this [LocalDate] and the provided [LocalDate]
         */
        fun LocalDate.monthsBetween(date: LocalDate) : List<YearMonth> {
            val firstMonth = YearMonth.from(this)
            if(this == date) return listOf(firstMonth)
            val secondMonth = YearMonth.from(date)
            var beginMonth : YearMonth = firstMonth
            var endMonth : YearMonth = secondMonth
            if(firstMonth.isAfter(secondMonth)){ //order months
                beginMonth = secondMonth
                endMonth = firstMonth
            }
            val monthsBetween = mutableListOf<YearMonth>()
            while (beginMonth.isBefore(endMonth) || beginMonth == endMonth){
                monthsBetween.add(beginMonth)
                beginMonth = beginMonth.plusMonths(1)
            }
            return Collections.unmodifiableList(monthsBetween)
        }

        /**
         * This function applied an action to a range of [LocalDate]s.
         */
        fun LocalDate.onRange(date: LocalDate, action : (LocalDate) -> LocalDate){
            var startDate = this
            var endDate = date
            if(date.isBefore(this)){
                startDate = date
                endDate = this
            }
            while (startDate.isBefore(endDate) || startDate == endDate) {
                startDate = action.invoke(startDate)
            }
        }
    }
}