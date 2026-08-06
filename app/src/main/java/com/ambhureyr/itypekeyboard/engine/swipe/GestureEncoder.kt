package com.ambhureyr.itypekeyboard.engine.swipe

import com.ambhureyr.itypekeyboard.engine.model.GesturePoint
import com.ambhureyr.itypekeyboard.engine.model.KeyModel

class GestureEncoder(private val spatialIndex: KeyboardSpatialIndex) {

    fun encode(points: List<GesturePoint>): String {
        val sequence = StringBuilder()
        var lastKey: KeyModel? = null

        for (pt in points) {
            val key = spatialIndex.findKeyAt(pt.x, pt.y)
            if (key != null && key.label.length == 1 && key.label[0].isLetter()) {
                val char = key.label[0].lowercaseChar()
                if (lastKey != key) {
                    if (sequence.isEmpty() || sequence.last() != char) {
                        sequence.append(char)
                    }
                    lastKey = key
                }
            }
        }
        return sequence.toString()
    }
}
