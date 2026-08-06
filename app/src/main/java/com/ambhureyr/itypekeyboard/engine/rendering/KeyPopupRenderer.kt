package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.ambhureyr.itypekeyboard.engine.model.KeyType
import com.ambhureyr.itypekeyboard.engine.theme.Colors
import com.ambhureyr.itypekeyboard.engine.theme.CustomTypeface

class KeyPopupRenderer : Renderer {

    private val popupPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyBackground
        setShadowLayer(10f, 0f, 5f, 0x55000000)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyText
        textAlign = Paint.Align.CENTER
        textSize = 48f
    }

    override fun render(context: RenderContext) {
        val pressed = context.pressedKey ?: return

        // Exclude Space, Function keys, Emoji, Shift, Enter, Backspace, and
        // kaomoji keys (a floating preview of a whole kaomoji string doesn't
        // read well at popup size) from popups.
        if (pressed.type == KeyType.SPACE ||
            pressed.primaryCode == -1 ||
            pressed.primaryCode == 10 ||
            pressed.primaryCode == -5 ||
            pressed.primaryCode == -4 ||
            pressed.insertText != null) {
            return
        }

        textPaint.typeface = CustomTypeface.getSemiBold(context.context)

        var displayText = pressed.label
        if (context.isShifted && displayText.length == 1 && displayText[0].isLetter()) {
            displayText = displayText.uppercase()
        }

        val keyRect = pressed.bounds
        val popupWidth = keyRect.width() * 1.35f
        val popupHeight = keyRect.height() * 1.85f

        // Position popup right above the key
        var top = keyRect.top - popupHeight - 8f
        var bottom = keyRect.top - 4f
        
        // Prevent top clipping
        if (top < 4f) {
            val offset = 4f - top
            top += offset
            bottom += offset
        }

        val left = keyRect.centerX() - popupWidth / 2f
        val right = keyRect.centerX() + popupWidth / 2f

        val popupRect = RectF(left, top, right, bottom)
        val radius = 24f

        // Draw iOS-style bulbous rounded popup with a small pointer contour at the bottom pointing to the key
        val path = Path().apply {
            addRoundRect(popupRect, radius, radius, Path.Direction.CW)
            // Add small bottom pointer triangle pointing to key center
            moveTo(keyRect.centerX() - 12f, bottom)
            lineTo(keyRect.centerX(), bottom + 10f)
            lineTo(keyRect.centerX() + 12f, bottom)
            close()
        }

        context.canvas.drawPath(path, popupPaint)

        val x = popupRect.centerX()
        val y = popupRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f - 4f

        context.canvas.drawText(
            displayText,
            x,
            y,
            textPaint
        )
    }
}
