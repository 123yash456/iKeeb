package com.ambhureyr.itypekeyboard.engine.model

import android.graphics.RectF

class KeyboardLayout {

    val rows: List<List<KeyModel>> = listOf(
        // Row 0: 1 2 3 4 5 6 7 8 9 0 (10 keys)
        listOf(
            KeyModel(49, "1"), KeyModel(50, "2"), KeyModel(51, "3"), KeyModel(52, "4"),
            KeyModel(53, "5"), KeyModel(54, "6"), KeyModel(55, "7"), KeyModel(56, "8"),
            KeyModel(57, "9"), KeyModel(48, "0")
        ),
        // Row 1: q w e r t y u i o p (10 keys)
        listOf(
            KeyModel(113, "q"), KeyModel(119, "w"), KeyModel(101, "e"), KeyModel(114, "r"),
            KeyModel(116, "t"), KeyModel(121, "y"), KeyModel(117, "u"), KeyModel(105, "i"),
            KeyModel(111, "o"), KeyModel(112, "p")
        ),
        // Row 2: a s d f g h j k l (9 keys - matched width with 10-key rows and balanced side margins)
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

    fun measure(width: Float, height: Float, horizontalPadding: Float = 6f, verticalPadding: Float = 5f) {
        if (width <= 0 || height <= 0) return

        val rowCount = rows.size
        // Slightly reduce row height by adding a little top/bottom breathing room or vertical padding
        val availableHeight = height - (verticalPadding * (rowCount + 1))
        val rowHeight = (availableHeight / rowCount) * 0.95f // Reduce key height by a little

        var currentY = verticalPadding + ((availableHeight - (rowHeight * rowCount)) / 2f)

        // Standard unit width calculated based on 10 keys (Row 0 & Row 1)
        val standard10KeyTotalFlex = 10f
        val availableWidth = width - (horizontalPadding * 11)
        val standardUnitWidth = availableWidth / standard10KeyTotalFlex

        rows.forEachIndexed { index, row ->
            if (index == 2) {
                // Row 2 (a-l, 9 keys): match key width to standard 10-key width, and center with side spacing
                val rowKeysCount = row.size
                val totalRowKeysWidth = rowKeysCount * standardUnitWidth
                val totalGapsWidth = (rowKeysCount - 1) * horizontalPadding
                val rowTotalWidth = totalRowKeysWidth + totalGapsWidth
                
                // Left and right symmetric padding to center the 9 keys
                val sideMargin = (width - rowTotalWidth) / 2f
                var currentX = sideMargin

                row.forEach { key ->
                    key.bounds = RectF(
                        currentX,
                        currentY,
                        currentX + standardUnitWidth,
                        currentY + rowHeight
                    )
                    currentX += standardUnitWidth + horizontalPadding
                }
            } else {
                val totalFlexWidth = row.sumOf { it.flexWidth.toDouble() }.toFloat()
                val rowAvailableWidth = width - (horizontalPadding * (row.size + 1))
                val unitWidth = rowAvailableWidth / totalFlexWidth

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
