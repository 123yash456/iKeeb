package com.ambhureyr.itypekeyboard

import android.content.Context
import android.graphics.Canvas
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

import android.graphics.Color
import android.graphics.Paint

class IKeebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : KeyboardView(context, attrs) {

    private val testPaint = Paint().apply{
        color = Color.parseColor("#007AFF")
        isAntiAlias= true
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(
            100f,
            100f,
            25f,
            testPaint
        )
    }
}