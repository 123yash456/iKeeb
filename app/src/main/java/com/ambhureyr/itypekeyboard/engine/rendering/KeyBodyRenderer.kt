package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import com.ambhureyr.itypekeyboard.engine.model.KeyType
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import com.ambhureyr.itypekeyboard.engine.theme.Colors
import com.ambhureyr.itypekeyboard.engine.theme.Dimensions

class KeyBodyRenderer : Renderer {

    private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.Key
    }

    private val functionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.FunctionKey
    }

    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyPressed
    }

    private val activeShiftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.Key // Active dark-filled shift background
    }

    override fun render(context: RenderContext) {
        context.layout.rows.forEach { row ->
            row.forEach { key ->
                val isPressed = context.pressedKey == key
                val isActiveShift = key.primaryCode == KeyboardLayout.KEYCODE_SHIFT && context.isShifted

                val paint = when {
                    isPressed -> pressedPaint
                    isActiveShift -> activeShiftPaint
                    key.type == KeyType.FUNCTION -> functionPaint
                    else -> normalPaint
                }

                context.canvas.drawRoundRect(
                    key.bounds,
                    Dimensions.CornerRadius,
                    Dimensions.CornerRadius,
                    paint
                )
            }
        }
    }
}
