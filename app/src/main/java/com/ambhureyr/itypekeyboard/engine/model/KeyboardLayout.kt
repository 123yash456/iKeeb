package com.ambhureyr.itypekeyboard.engine.model

import android.content.Context
import android.graphics.RectF
import com.ambhureyr.itypekeyboard.R

class KeyboardLayout {

    val rows: List<List<KeyModel>> = listOf(
        // Row 1: q w e r t y u i o p
        listOf(
            KeyModel(113, "q"), KeyModel(119, "w"), KeyModel(101, "e"), KeyModel(114, "r"),
            KeyModel(116, "t"), KeyModel(121, "y"), KeyModel(117, "u"), KeyModel(105, "i"),
            KeyModel(111, "o"), KeyModel(112, "p")
        ),
        // Row 2: a s d f g h j k l
        listOf(
            KeyModel(97, "a"), KeyModel(115, "s"), KeyModel(100, "d"), KeyModel(102, "f"),
            KeyModel(103, "g"), KeyModel(104, "h"), KeyModel(106, "j"), KeyModel(107, "k"),
            KeyModel(108, "l")
        ),
        // Row 3: Shift z x c v b n m Backspace
        listOf(
            KeyModel(-1, "Shift", KeyType.FUNCTION, flexWidth = 1.5f),
            KeyModel(122, "z"), KeyModel(120, "x"), KeyModel(99, "c"), KeyModel(118, "v"),
            KeyModel(98, "b"), KeyModel(110, "n"), KeyModel(109, "m"),
            KeyModel(-5, "⌫", KeyType.FUNCTION, flexWidth = 1.5f, isRepeatable = true)
        ),
        // Row 4: Space & Enter
        listOf(
            KeyModel(32, "space", KeyType.SPACE, flexWidth = 6.5f),
            KeyModel(10, "↵", KeyType.FUNCTION, flexWidth = 2.5f)
        )
    )

    fun measure(width: Float, height: Float, horizontalPadding: Float = 8f, verticalPadding: Float = 8f) {
        if (width <= 0 || height <= 0) return

        val rowCount = rows.size
        val availableHeight = height - (verticalPadding * (rowCount + 1))
        val rowHeight = availableHeight / rowCount

        var currentY = verticalPadding

        rows.forEach { row ->
            val totalFlexWidth = row.sumOf { it.flexWidth.toDouble() }.toFloat()
            val availableWidth = width - (horizontalPadding * (row.size + 1))
            val unitWidth = availableWidth / totalFlexWidth

            var currentX = horizontalPadding

            row.forEach { key ->
                val keyWidth = unitWidth * key.flexWidth
                key.bounds = RectF(
                    currentX,
                    currentY,
                    currentX + keyWidth,
                    currentY + rowHeight
                )
                currentX += keyWidth + horizontalPadding
            }

            currentY += rowHeight + verticalPadding
        }
    }

    fun findKeyAt(x: Float, y: Float): KeyModel? {
        rows.forEach { row ->
            row.forEach { key ->
                if (key.bounds.contains(x, y)) {
                    return key
                }
            }
        }
        return null
    }

    companion object {
        const val KEYCODE_SHIFT = -1
        const val KEYCODE_DELETE = -5
        const val KEYCODE_SPACE = 32
        const val KEYCODE_ENTER = 10
    }
}
