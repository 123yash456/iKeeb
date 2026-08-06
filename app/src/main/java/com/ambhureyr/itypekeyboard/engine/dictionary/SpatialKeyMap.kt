package com.ambhureyr.itypekeyboard.engine.dictionary

import android.graphics.PointF
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import kotlin.math.hypot

/**
 * Key-to-key distance lookup built from this device's *actual* measured key
 * positions (via [KeyboardLayout.keyCenter]) -- not a hardcoded pixel grid
 * that assumes one specific screen size.
 *
 * Call [refresh] after the layout is (re)measured, e.g. from
 * `onSizeChanged`, so distances track rotation/resize. It's safe to call
 * `refresh` at any time, including while the symbol keyboard is showing --
 * if letter positions aren't currently available it just keeps the last
 * known-good values instead of wiping itself out.
 */
class SpatialKeyMap(private val keyboardLayout: KeyboardLayout) {

    private var keyCoords: Map<Char, PointF> = emptyMap()
    private var keyPitch: Float = 1f // ~distance between horizontally-adjacent keys, used to normalize scores

    fun refresh() {
        val coords = mutableMapOf<Char, PointF>()
        for (c in 'a'..'z') {
            keyboardLayout.keyCenter(c)?.let { coords[c] = it }
        }
        // Layout isn't in a letter-bearing state right now (e.g. symbol mode
        // mid-toggle) -- keep whatever we last measured rather than going blank.
        if (coords.size < 20) return

        keyCoords = coords
        keyPitch = estimatePitch(coords)
    }

    private fun estimatePitch(coords: Map<Char, PointF>): Float {
        val topRow = "qwertyuiop"
        val gaps = mutableListOf<Float>()
        for (i in 0 until topRow.length - 1) {
            val p1 = coords[topRow[i]] ?: continue
            val p2 = coords[topRow[i + 1]] ?: continue
            gaps.add(hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()).toFloat())
        }
        return if (gaps.isNotEmpty()) gaps.average().toFloat().coerceAtLeast(1f) else 1f
    }

    /** Distance between two letters' keys, normalized so ~1.0 == one key-width apart. */
    fun distance(c1: Char, c2: Char): Float {
        val lc1 = c1.lowercaseChar()
        val lc2 = c2.lowercaseChar()
        if (lc1 == lc2) return 0f
        val p1 = keyCoords[lc1] ?: return 3f
        val p2 = keyCoords[lc2] ?: return 3f
        val raw = hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()).toFloat()
        return raw / keyPitch
    }
}
