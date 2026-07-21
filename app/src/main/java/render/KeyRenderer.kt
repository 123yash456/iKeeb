package com.ambhureyr.itypekeyboard.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.inputmethodservice.Keyboard
import android.graphics.Typeface
import com.ambhureyr.itypekeyboard.theme.Colors
import com.ambhureyr.itypekeyboard.theme.Dimensions

object KeyRenderer {

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyBackground
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyText
        textAlign = Paint.Align.CENTER
        textSize = 42f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    fun drawKey(
        canvas: Canvas,
        key: Keyboard.Key
    ) {

        val rect = RectF(
            key.x.toFloat(),
            key.y.toFloat(),
            (key.x + key.width).toFloat(),
            (key.y + key.height).toFloat()
        )

        canvas.drawRoundRect(
            rect,
            Dimensions.CornerRadius,
            Dimensions.CornerRadius,
            keyPaint
        )

        key.label?.let { label ->

            val x = rect.centerX()

            val y = rect.centerY() -
                    (textPaint.descent() + textPaint.ascent()) / 2

            canvas.drawText(
                label.toString(),
                x,
                y,
                textPaint
            )
        }
    }
}