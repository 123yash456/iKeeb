package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Canvas
import com.ambhureyr.itypekeyboard.engine.model.KeyModel
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout

data class RenderContext(
    val canvas: Canvas,
    val layout: KeyboardLayout,
    val width: Float,
    val height: Float,
    val isShifted: Boolean = false,
    val pressedKey: KeyModel? = null
)
