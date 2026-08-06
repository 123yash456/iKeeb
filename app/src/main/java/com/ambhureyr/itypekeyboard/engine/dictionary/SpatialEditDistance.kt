package com.ambhureyr.itypekeyboard.engine.dictionary

/**
 * Alignment-correct, spatially-weighted string distance used by both slide
 * typing and autocorrect.
 *
 * Three things matter here, each fixing a real failure mode:
 * 1. Proper alignment (Levenshtein), not index-by-index comparison -- one
 *    dropped or extra letter shouldn't wreck the score for everything after it.
 * 2. Substitution cost = actual on-screen key distance (via [SpatialKeyMap]),
 *    so nudging to a neighboring key is nearly free but jumping across the
 *    keyboard is expensive.
 * 3. An explicit transposition operation (Damerau-Levenshtein) -- swapped
 *    adjacent letters ("teh") are the single most common typo shape and plain
 *    edit distance handles them badly without this.
 *
 * The result is normalized by the longer string's length so a short
 * candidate can't win purely by being short (deleting down to a 2-letter
 * word is "cheap" in raw edit distance but usually the wrong answer).
 */
object SpatialEditDistance {

    private const val INS_DEL_COST = 1.3f  // dropping/adding a whole letter
    private const val TRANSPOSE_COST = 1.0f // swapping two adjacent letters

    fun compute(a: String, b: String, spatialKeyMap: SpatialKeyMap): Float {
        val la = a.length
        val lb = b.length
        if (la == 0) return lb.toFloat()
        if (lb == 0) return la.toFloat()

        val dp = Array(la + 1) { FloatArray(lb + 1) }
        for (i in 0..la) dp[i][0] = i * INS_DEL_COST
        for (j in 0..lb) dp[0][j] = j * INS_DEL_COST

        for (i in 1..la) {
            for (j in 1..lb) {
                val subCost = spatialKeyMap.distance(a[i - 1], b[j - 1])
                var best = minOf(
                    dp[i - 1][j - 1] + subCost,
                    dp[i - 1][j] + INS_DEL_COST,
                    dp[i][j - 1] + INS_DEL_COST
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    best = minOf(best, dp[i - 2][j - 2] + TRANSPOSE_COST)
                }
                dp[i][j] = best
            }
        }

        return dp[la][lb] / maxOf(la, lb)
    }
}
