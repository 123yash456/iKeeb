package com.ambhureyr.itypekeyboard.engine.swipe

import com.ambhureyr.itypekeyboard.engine.model.GesturePoint

class GestureTracker {
    private val points = mutableListOf<GesturePoint>()

    fun reset() {
        points.clear()
    }

    fun addPoint(x: Float, y: Float, timestamp: Long) {
        points.add(GesturePoint(x, y, timestamp))
    }

    fun getPoints(): List<GesturePoint> = points
}

class PathSimplifier {
    fun simplify(points: List<GesturePoint>, tolerance: Float = 5f): List<GesturePoint> {
        if (points.size <= 2) return points
        val simplified = mutableListOf<GesturePoint>()
        simplified.add(points.first())
        for (i in 1 until points.size - 1) {
            val prev = simplified.last()
            val curr = points[i]
            val dist = kotlin.math.hypot((curr.x - prev.x).toDouble(), (curr.y - prev.y).toDouble()).toFloat()
            if (dist >= tolerance) {
                simplified.add(curr)
            }
        }
        simplified.add(points.last())
        return simplified
    }
}

class GestureNormalizer {
    fun normalize(points: List<GesturePoint>, width: Float, height: Float): List<GesturePoint> {
        if (width <= 0f || height <= 0f) return points
        return points.map { p ->
            GesturePoint(p.x / width, p.y / height, p.timestamp)
        }
    }
}
