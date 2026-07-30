package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import com.ambhureyr.itypekeyboard.engine.model.KeyType
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

    override fun render(context: RenderContext) {
        context.layout.rows.forEach { row ->
            row.forEach { key ->
                val isPressed = context.pressedKey == key
                val paint = when {
                    isPressed -> pressedPaint
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
