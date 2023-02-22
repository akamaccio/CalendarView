package net.akamaccio.widget

import android.widget.TextView
import java.time.LocalDate

/**
 * Created by Matteo Maccioni on 15/02/2023.
 *
 * Interface for calendar day decoration.
 */
interface MmCalendarDayDecorator {
    /**
     * Returns TRUE if the day must be decorated, FALSE otherwise.
     */
    fun shouldDecorate(date: LocalDate): Boolean

    /**
     * Decorates the calendar day [TextView].
     */
    fun decorate(view: TextView)
}