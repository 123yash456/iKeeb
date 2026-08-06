package com.ambhureyr.itypekeyboard.engine.dictionary

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isWord = false
    var word: String? = null
    var frequencyRank: Int = Int.MAX_VALUE
}
