package com.ambhureyr.itypekeyboard.engine.theme

import android.content.Context
import android.graphics.Typeface
import android.util.Log

/**
 * Loads and caches the keyboard's UI typeface (Inter) from assets.
 *
 * Two static weights ship in assets/fonts:
 *  - Inter-Regular.ttf   (wght 400) for normal key labels
 *  - Inter-SemiBold.ttf  (wght 600) for emphasis (active shift, popups)
 *
 * Both are instances cut from the official variable font, so they stay
 * true to Inter's letterforms instead of relying on Android's synthetic
 * bold (which just skews/thickens the regular glyphs).
 */
object CustomTypeface {
    private var cachedRegular: Typeface? = null
    private var cachedSemiBold: Typeface? = null

    fun get(context: Context?): Typeface {
        cachedRegular?.let { return it }
        if (context == null) return Typeface.DEFAULT

        val loaded = try {
            Typeface.createFromAsset(context.assets, "fonts/Inter-Regular.ttf")
        } catch (e: Exception) {
            Log.e("CustomTypeface", "Failed to load Inter-Regular from assets", e)
            Typeface.DEFAULT
        }
        cachedRegular = loaded
        return loaded
    }

    fun getSemiBold(context: Context?): Typeface {
        cachedSemiBold?.let { return it }
        if (context == null) return Typeface.DEFAULT_BOLD

        val loaded = try {
            Typeface.createFromAsset(context.assets, "fonts/Inter-SemiBold.ttf")
        } catch (e: Exception) {
            Log.e("CustomTypeface", "Failed to load Inter-SemiBold from assets", e)
            Typeface.DEFAULT_BOLD
        }
        cachedSemiBold = loaded
        return loaded
    }
}
