package com.ambhureyr.itypekeyboard.engine.slide

import com.ambhureyr.itypekeyboard.engine.dictionary.Dictionary
import com.ambhureyr.itypekeyboard.engine.dictionary.SpatialEditDistance
import com.ambhureyr.itypekeyboard.engine.dictionary.SpatialKeyMap
import com.ambhureyr.itypekeyboard.engine.dictionary.UserDictionary
import com.ambhureyr.itypekeyboard.engine.model.KeyModel
import kotlin.math.abs

class SlideDictionaryDecoder(
    private val dictionary: Dictionary,
    private val userDict: UserDictionary,
    private val spatialKeyMap: SpatialKeyMap
) {

    fun decode(traversedKeys: List<KeyModel>): String? {
        if (traversedKeys.isEmpty()) return null

        val sequence = buildDedupedSequence(traversedKeys)
        if (sequence.isEmpty()) return null
        if (sequence.length < 3) return sequence

        if (userDict.contains(sequence)) return sequence
        if (dictionary.contains(sequence)) return sequence
        if (dictionary.words.isEmpty()) return sequence

        var bestWord: String? = null
        var bestScore = Float.MAX_VALUE

        // Personal dictionary gets a head start since these are words this user actually uses.
        for (word in userDict.getWords()) {
            if (abs(word.length - sequence.length) > 4) continue
            val score = SpatialEditDistance.compute(sequence, dedupeConsecutive(word), spatialKeyMap) * 0.7f
            if (score < bestScore) {
                bestScore = score
                bestWord = word
            }
        }

        for (word in dictionary.words) {
            if (abs(word.length - sequence.length) > 4) continue

            val wordKeySeq = dedupeConsecutive(word)
            var score = SpatialEditDistance.compute(sequence, wordKeySeq, spatialKeyMap)

            if (wordKeySeq.first() != sequence.first()) score += 0.35f
            if (wordKeySeq.last() != sequence.last()) score += 0.2f
            score += dictionary.rankOf(word) / 40000f // gentle frequency tie-breaker

            if (score < bestScore) {
                bestScore = score
                bestWord = word
            }
        }

        // Nothing reasonably close was found -- fall back to the raw traversed
        // letters instead of forcing an unrelated word on the user.
        val confidenceThreshold = 0.85f
        if (bestWord == null || bestScore > confidenceThreshold) return sequence

        // Only reinforce words we're genuinely confident about. Learning the raw
        // fallback sequence (or a shaky guess) would permanently pollute future
        // swipes, since the personal dictionary is checked first and favored.
        userDict.addWord(bestWord)
        return bestWord
    }

    private fun buildDedupedSequence(traversedKeys: List<KeyModel>): String {
        val sequence = StringBuilder()
        traversedKeys.forEach { key ->
            if (key.label.length == 1 && key.label[0].isLetter()) {
                val char = key.label[0].lowercaseChar()
                if (sequence.isEmpty() || sequence.last() != char) {
                    sequence.append(char)
                }
            }
        }
        return sequence.toString()
    }

    private fun dedupeConsecutive(word: String): String {
        val sb = StringBuilder()
        for (c in word) {
            if (sb.isEmpty() || sb.last() != c) sb.append(c)
        }
        return sb.toString()
    }
}
