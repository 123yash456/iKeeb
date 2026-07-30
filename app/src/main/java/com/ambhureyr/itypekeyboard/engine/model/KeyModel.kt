package com.ambhureyr.itypekeyboard.engine.model

import android.graphics.RectF

enum class KeyType {
    NORMAL,
    FUNCTION,
    SPACE
}

data class KeyModel(
    val primaryCode: Int,
    val label: String,
    val type: KeyType = KeyType.NORMAL,
    val flexWidth: Float = 1f, // Relative width ratio within row
    val isRepeatable: Boolean = false,
    var bounds: RectF = RectF()
)
