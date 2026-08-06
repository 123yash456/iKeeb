package com.ambhureyr.itypekeyboard.engine.dictionary

import android.content.Context
import android.content.SharedPreferences

class UserDictionary(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("ikeeb_user_dict", Context.MODE_PRIVATE)
    private val customWords = mutableSetOf<String>()

    init {
        val saved = prefs.getStringSet("words", emptySet()) ?: emptySet()
        customWords.addAll(saved)
    }

    fun addWord(word: String) {
        val lower = word.lowercase()
        if (lower.length >= 2 && customWords.add(lower)) {
            prefs.edit().putStringSet("words", customWords).apply()
        }
    }

    fun contains(word: String): Boolean = customWords.contains(word.lowercase())

    fun getWords(): Set<String> = customWords
}
