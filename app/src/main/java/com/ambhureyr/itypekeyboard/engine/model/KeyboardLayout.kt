package com.ambhureyr.itypekeyboard.engine.model

import android.graphics.RectF

class KeyboardLayout {

    var isSymbolMode: Boolean = false

    private val qwertyRows: List<List<KeyModel>> = listOf(
        // Row 0: 1 2 3 4 5 6 7 8 9 0
        listOf(
            KeyModel(49, "1"), KeyModel(50, "2"), KeyModel(51, "3"), KeyModel(52, "4"),
            KeyModel(53, "5"), KeyModel(54, "6"), KeyModel(55, "7"), KeyModel(56, "8"),
            KeyModel(57, "9"), KeyModel(48, "0")
        ),
        // Row 1: q w e r t y u i o p
        listOf(
            KeyModel(113, "q"), KeyModel(119, "w"), KeyModel(101, "e"), KeyModel(114, "r"),
            KeyModel(116, "t"), KeyModel(121, "y"), KeyModel(117, "u"), KeyModel(105, "i"),
            KeyModel(111, "o"), KeyModel(112, "p")
        ),
        // Row 2: a s d f g h j k l (9 keys)
        listOf(
            KeyModel(97, "a"), KeyModel(115, "s"), KeyModel(100, "d"), KeyModel(102, "f"),
            KeyModel(103, "g"), KeyModel(104, "h"), KeyModel(106, "j"), KeyModel(107, "k"),
            KeyModel(108, "l")
        ),
        // Row 3: Shift z x c v b n m Backspace
        listOf(
            KeyModel(-1, "⇧", KeyType.FUNCTION, flexWidth = 1.4f),
            KeyModel(122, "z"), KeyModel(120, "x"), KeyModel(99, "c"), KeyModel(118, "v"),
            KeyModel(98, "b"), KeyModel(110, "n"), KeyModel(109, "m"),
            KeyModel(-5, "⌫", KeyType.FUNCTION, flexWidth = 1.4f, isRepeatable = true)
        ),
        // Row 4: 123 switch, Space & Enter
        listOf(
            KeyModel(-2, "123", KeyType.FUNCTION, flexWidth = 2.0f),
            KeyModel(32, "space", KeyType.SPACE, flexWidth = 5.5f),
            KeyModel(10, "return", KeyType.FUNCTION, flexWidth = 2.5f)
        )
    )

    private val symbolRows: List<List<KeyModel>> = listOf(
        // Row 0: - / : ; ( ) $ & @ "
        listOf(
            KeyModel(45, "-"), KeyModel(47, "/"), KeyModel(58, ":"), KeyModel(59, ";"),
            KeyModel(40, "("), KeyModel(41, ")"), KeyModel(36, "$"), KeyModel(38, "&"),
            KeyModel(64, "@"), KeyModel(34, "\"")
        ),
        // Row 1: . , ? ! ~ ` + = \ |
        listOf(
            KeyModel(46, "."), KeyModel(44, ","), KeyModel(63, "?"), KeyModel(33, "!"),
            KeyModel(126, "~"), KeyModel(96, "`"), KeyModel(43, "+"), KeyModel(61, "="),
            KeyModel(92, "\\"), KeyModel(124, "|")
        ),
        // Row 2: [ ] { } # % ^ * _ -
        listOf(
            KeyModel(91, "["), KeyModel(93, "]"), KeyModel(123, "{"), KeyModel(125, "}"),
            KeyModel(35, "#"), KeyModel(37, "%"), KeyModel(94, "^"), KeyModel(42, "*"),
            KeyModel(95, "_"), KeyModel(45, "-")
        ),
        // Row 3: Switch back, < > € £ ¥ • , . Backspace
        listOf(
            KeyModel(-3, "ABC", KeyType.FUNCTION, flexWidth = 1.5f),
            KeyModel(60, "<"), KeyModel(62, ">"), KeyModel(8364, "€"), KeyModel(163, "£"),
            KeyModel(165, "¥"), KeyModel(8226, "•"), KeyModel(44, ","), KeyModel(46, "."),
            KeyModel(-5, "⌫", KeyType.FUNCTION, flexWidth = 1.5f, isRepeatable = true)
        ),
        // Row 4: ABC switch, Space & Enter
        listOf(
            KeyModel(-2, "ABC", KeyType.FUNCTION, flexWidth = 2.0f),
            KeyModel(32, "space", KeyType.SPACE, flexWidth = 5.5f),
            KeyModel(10, "return", KeyType.FUNCTION, flexWidth = 2.5f)
        )
    )

    val rows: List<List<KeyModel>>
        get() = if (isSymbolMode) symbolRows else qwertyRows

    fun measure(width: Float, height: Float, horizontalPadding: Float = 6f, verticalPadding: Float = 5f) {
        if (width <= 0 || height <= 0) return

        val activeRows = rows
        val rowCount = activeRows.size
        val availableHeight = height - (verticalPadding * (rowCount + 1))
        val rowHeight = (availableHeight / rowCount) * 0.90f

        var currentY = verticalPadding + ((availableHeight - (rowHeight * rowCount)) / 2f)

        // Standard unit width calculated based on 10 keys (Row 0 & Row 1)
        val standard10KeyTotalFlex = 10f
        val availableWidth = width - (horizontalPadding * 11)
        val standardUnitWidth = availableWidth / standard10KeyTotalFlex

        activeRows.forEachIndexed { index, row ->
            // In QWERTY mode, row 2 has 9 keys. In symbol mode, row 2 has 10 keys.
            if (!isSymbolMode && index == 2) {
                val rowKeysCount = row.size
                val totalRowKeysWidth = rowKeysCount * standardUnitWidth
                val totalGapsWidth = (rowKeysCount - 1) * horizontalPadding
                val rowTotalWidth = totalRowKeysWidth + totalGapsWidth
                
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
        const val KEYCODE_MODE_SYMBOL = -2
        const val KEYCODE_MODE_ABC = -3
    }
}
