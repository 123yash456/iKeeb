package com.ambhureyr.itypekeyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import com.ambhureyr.itypekeyboard.render.KeyRenderer
import com.ambhureyr.itypekeyboard.theme.Colors

class IKeebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : KeyboardView(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.KeyBackground
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        keyboard?.keys?.forEach { key ->
            KeyRenderer.drawKey(canvas, key)
        }
    }
}