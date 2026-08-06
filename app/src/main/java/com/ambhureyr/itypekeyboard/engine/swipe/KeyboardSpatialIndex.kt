package com.ambhureyr.itypekeyboard.engine.swipe

import com.ambhureyr.itypekeyboard.engine.model.KeyModel
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import kotlin.math.hypot

class KeyboardSpatialIndex(private val layout: KeyboardLayout) {

    fun findKeyAt(x: Float, y: Float): KeyModel? {
        return layout.findKeyAt(x, y)
    }

    fun distanceToKey(x: Float, y: Float, key: KeyModel): Float {
        val cx = key.bounds.centerX()
        val cy = key.bounds.centerY()
        return hypot((x - cx).toDouble(), (y - cy).toDouble()).toFloat()
    }
}
