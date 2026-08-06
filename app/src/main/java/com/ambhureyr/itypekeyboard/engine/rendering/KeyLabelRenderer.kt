package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import com.ambhureyr.itypekeyboard.engine.theme.Colors
import com.ambhureyr.itypekeyboard.engine.theme.CustomTypeface
import com.ambhureyr.itypekeyboard.engine.theme.Dimensions

class KeyLabelRenderer : Renderer {

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyText
        textAlign = Paint.Align.CENTER
    }

    // Used for the active-shift arrow and other keys that want emphasis --
    // a real SemiBold cut of Inter rather than Android's synthetic bold.
    private val emphasisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyText
        textAlign = Paint.Align.CENTER
    }

    // Reused for width measurement when shrinking long kaomoji labels to fit.
    private val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun render(context: RenderContext) {
        val ctx = context.context
        val regularFont = CustomTypeface.get(ctx)
        val semiBoldFont = CustomTypeface.getSemiBold(ctx)
        labelPaint.typeface = regularFont
        emphasisPaint.typeface = semiBoldFont
        measurePaint.typeface = regularFont

        val fontSize = Dimensions.getKeyTextSize(context.context)
        labelPaint.textSize = fontSize
        emphasisPaint.textSize = fontSize * 1.2f

        context.layout.rows.forEach { row ->
            row.forEach { key ->
                var displayText = key.label
                var paintToUse = labelPaint

                if (key.primaryCode == KeyboardLayout.KEYCODE_SHIFT) {
                    paintToUse = emphasisPaint
                    displayText = if (context.isShifted) "⬆" else "⇧"
                } else if (key.primaryCode == KeyboardLayout.KEYCODE_EMOJI_NEXT_PAGE) {
                    // Show the current page as "2/3" instead of a static arrow
                    // so paging through the text-emoji panel has feedback.
                    displayText = "${context.layout.emojiPage + 1}/${context.layout.emojiPageCount}"
                } else if (context.isShifted && displayText.length == 1 && displayText[0].isLetter()) {
                    displayText = displayText.uppercase()
                }

                val availableWidth = key.bounds.width() * 0.86f
                val fittedSize = fitTextSize(displayText, paintToUse.textSize, availableWidth)
                val originalSize = paintToUse.textSize
                paintToUse.textSize = fittedSize

                val x = key.bounds.centerX()
                val y = key.bounds.centerY() - (paintToUse.descent() + paintToUse.ascent()) / 2f

                context.canvas.drawText(
                    displayText,
                    x,
                    y,
                    paintToUse
                )

                paintToUse.textSize = originalSize
            }
        }
    }

    /**
     * Shrinks [size] down (never up) until [text] fits within [maxWidth],
     * stopping at a readable floor. Only kicks in for longer labels like
     * kaomoji -- single characters and short function labels are untouched.
     */
    private fun fitTextSize(text: String, size: Float, maxWidth: Float): Float {
        if (text.length <= 2 || maxWidth <= 0f) return size

        val minSize = size * 0.45f
        var candidate = size
        measurePaint.textSize = candidate

        while (measurePaint.measureText(text) > maxWidth && candidate > minSize) {
            candidate -= 1f
            measurePaint.textSize = candidate
        }
        return candidate
    }
}
