package com.ambhureyr.itypekeyboard.engine.theme

import android.content.Context

object Dimensions {
    const val CornerRadius = 18f
    const val ShadowOffset = 3f
    const val ShadowBlur = 6f

    fun getKeyTextSize(context: Context?): Float {
        val baseSize = 42f
        if (context == null) return baseSize
        val prefs = context.getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE)
        val fontScale = prefs.getInt("font_scale", 100) / 100f
        return baseSize * fontScale
    }
}
