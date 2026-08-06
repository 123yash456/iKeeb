package com.ambhureyr.itypekeyboard.engine.dictionary

import android.content.Context

/**
 * Loads and indexes the word list bundled at assets/dictionary.txt.
 *
 * The list is ordered most-common -> least-common (via wordfreq), so a word's
 * index in [words] doubles as a cheap frequency rank for tie-breaking between
 * otherwise-equally-good matches.
 */
class Dictionary private constructor(
    val words: List<String>
) {
    private val rank: Map<String, Int> = words.withIndex().associate { (i, w) -> w to i }

    fun contains(word: String): Boolean = rank.containsKey(word)

    /** Lower is more common. Returns Int.MAX_VALUE for unknown words. */
    fun rankOf(word: String): Int = rank[word] ?: Int.MAX_VALUE

    companion object {
        @Volatile private var instance: Dictionary? = null

        fun get(context: Context): Dictionary {
            return instance ?: synchronized(this) {
                instance ?: load(context).also { instance = it }
            }
        }

        private fun load(context: Context): Dictionary {
            val words = try {
                context.applicationContext.assets.open("dictionary.txt").bufferedReader().useLines { lines ->
                    lines.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            return Dictionary(words)
        }
    }
}
