package com.ambhureyr.itypekeyboard.engine.slide

import android.content.Context

object SlideCheck {
    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("slide_typing_enabled", true)
    }
}
