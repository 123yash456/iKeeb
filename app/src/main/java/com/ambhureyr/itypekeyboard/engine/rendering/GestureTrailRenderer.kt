package com.ambhureyr.itypekeyboard.engine.rendering

import android.graphics.Path

class GestureTrailRenderer : Renderer {

    private val trailPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x883B82F6.toInt() // Semi-transparent blue gesture trail
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    var currentPath: Path? = null

    override fun render(context: RenderContext) {
        val path = currentPath ?: return
        if (!path.isEmpty) {
            context.canvas.drawPath(path, trailPaint)
        }
    }
}
