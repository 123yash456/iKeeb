package com.ambhureyr.itypekeyboard.engine.swipe

import android.content.Context
import com.ambhureyr.itypekeyboard.engine.dictionary.Dictionary
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import kotlin.math.abs

class SwipeEngine(context: Context, private val layout: KeyboardLayout) {
    private val tracker = GestureTracker()
    private val simplifier = PathSimplifier()
    private val normalizer = GestureNormalizer()
    private val spatialIndex = KeyboardSpatialIndex(layout)
    private val encoder = GestureEncoder(spatialIndex)
    private val dictionary = Dictionary.get(context)

    fun startGesture() {
        tracker.reset()
    }

    fun addPoint(x: Float, y: Float, timestamp: Long) {
        tracker.addPoint(x, y, timestamp)
    }

    fun decodeGesture(width: Float, height: Float): String? {
        val raw = tracker.getPoints()
        if (raw.isEmpty()) return null

        val simplified = simplifier.simplify(raw)
        val normalized = normalizer.normalize(simplified, width, height)
        val signature = encoder.encode(simplified)
        if (signature.isEmpty()) return null

        if (dictionary.contains(signature)) return signature

        var bestWord: String? = null
        var bestScore = Int.MAX_VALUE

        for (word in dictionary.words) {
            if (abs(word.length - signature.length) > 3) continue
            val score = levenshtein(signature, word)
            if (score < bestScore) {
                bestScore = score
                bestWord = word
            }
        }

        return if (bestWord != null && bestScore <= 3) bestWord else signature
    }

    private fun levenshtein(a: String, b: String): Int {
        val la = a.length
        val lb = b.length
        if (la == 0) return lb
        if (lb == 0) return la

        var prev = IntArray(lb + 1) { it }
        var curr = IntArray(lb + 1)

        for (i in 1..la) {
            curr[0] = i
            for (j in 1..lb) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost
                )
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[lb]
    }
}
