package com.ambhureyr.itypekeyboard.engine.dictionary

import kotlin.math.abs

/**
 * Suggests a correction for a just-finished tap-typed word, using
 * spatially-weighted, alignment-correct matching (so a single dropped or
 * extra letter -- the most common typo shape -- still matches cleanly)
 * plus the user's own personal vocabulary.
 */
class Autocorrector(
    private val dictionary: Dictionary,
    private val userDict: UserDictionary,
    private val spatialKeyMap: SpatialKeyMap
) {

    fun correct(word: String): String? {
        if (word.length < 3) return null
        val lower = word.lowercase()
        if (userDict.contains(lower) || dictionary.contains(lower)) return null

        var bestWord: String? = null
        var bestScore = Float.MAX_VALUE

        // 1. Personal dictionary first -- words this user actually uses get priority.
        for (candidate in userDict.getWords()) {
            if (abs(candidate.length - lower.length) > 3) continue
            val score = SpatialEditDistance.compute(lower, candidate, spatialKeyMap) * 0.8f
            if (score < bestScore) {
                bestScore = score
                bestWord = candidate
            }
        }

        // 2. Main dictionary, spatially-weighted alignment + light frequency tie-break.
        // Unlike swipe decoding, we already have the whole typed word, so there's no
        // need for a first/last-letter heuristic -- full alignment is enough on its own,
        // and it's what correctly untangles common transposition typos like "teh" -> "the".
        for (candidate in dictionary.words) {
            if (abs(candidate.length - lower.length) > 3) continue

            val score = SpatialEditDistance.compute(lower, candidate, spatialKeyMap) +
                dictionary.rankOf(candidate) / 60000f

            if (score < bestScore) {
                bestScore = score
                bestWord = candidate
            }
        }

        val match = bestWord ?: return null
        if (bestScore > 0.65f) return null // not confident enough -- leave the word alone

        // Only learn corrections we're actually confident in, so a bad guess
        // can't start reinforcing itself on future typos.
        userDict.addWord(match)

        return if (word.first().isUpperCase()) {
            match.replaceFirstChar { it.uppercase() }
        } else {
            match
        }
    }
}
