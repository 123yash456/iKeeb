package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Paint
import com.ambhureyr.itypekeyboard.engine.theme.Colors

class BackgroundRenderer : Renderer {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyboardBackground
    }

    override fun render(context: RenderContext) {
        context.canvas.drawRect(
            0f,
            0f,
            context.width,
            context.height,
            bgPaint
        )
    }
}
