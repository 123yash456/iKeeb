package com.ambhureyr.itypekeyboard.engine.dictionary

class Trie {
    val root = TrieNode()

    fun insert(word: String, rank: Int) {
        var current = root
        for (char in word.lowercase()) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isWord = true
        current.word = word
        current.frequencyRank = rank
    }

    fun collectWithPrefix(prefix: String, maxResults: Int = 100): List<String> {
        val results = mutableListOf<String>()
        var current = root
        for (char in prefix.lowercase()) {
            current = current.children[char] ?: return results
        }
        dfs(current, results, maxResults)
        return results
    }

    private fun dfs(node: TrieNode, results: MutableList<String>, maxResults: Int) {
        if (results.size >= maxResults) return
        if (node.isWord && node.word != null) {
            results.add(node.word!!)
        }
        for ((_, child) in node.children) {
            dfs(child, results, maxResults)
        }
    }
}
