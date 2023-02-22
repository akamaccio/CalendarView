package it.matmacci.calendarview

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/**
 * Created by Matteo Maccioni on 17/02/2023.
 *
 * Span to draw a dot centered under a section of text.
 */
class DotSpan(
    private val radius : Float = 3f,
    private val color : Int = 0
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val oldColor = paint.color
        if(color != 0)
            paint.color = color
        canvas.drawCircle((left + right) / 2.0f, bottom + radius, radius, paint)
        paint.color = oldColor
    }
}