package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import android.graphics.Typeface
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import com.ambhureyr.itypekeyboard.engine.theme.Colors
import com.ambhureyr.itypekeyboard.engine.theme.Dimensions

class KeyLabelRenderer : Renderer {

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyText
        textAlign = Paint.Align.CENTER
        textSize = Dimensions.KeyTextSize
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    override fun render(context: RenderContext) {
        context.layout.rows.forEach { row ->
            row.forEach { key ->
                var displayText = key.label

                if (key.primaryCode == KeyboardLayout.KEYCODE_SHIFT && context.isShifted) {
                    displayText = "SHIFT"
                } else if (context.isShifted && displayText.length == 1 && displayText[0].isLetter()) {
                    displayText = displayText.uppercase()
                }

                val x = key.bounds.centerX()
                val y = key.bounds.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2f

                context.canvas.drawText(
                    displayText,
                    x,
                    y,
                    labelPaint
                )
            }
        }
    }
}
