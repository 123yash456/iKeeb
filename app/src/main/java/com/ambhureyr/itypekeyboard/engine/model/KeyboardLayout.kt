package com.ambhureyr.itypekeyboard.engine.model

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF

class KeyboardLayout {

    var isSymbolMode: Boolean = false
    var isEmojiMode: Boolean = false
        private set

    // Which mode to snap back to when the emoji panel's "ABC" key is tapped --
    // qwerty if the panel was opened from letters, symbols if opened from 123.
    private var emojiReturnsToSymbolMode: Boolean = false

    var emojiPage: Int = 0
        private set
    val emojiPageCount: Int get() = emojiPages.size

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
        // Row 4: 123 switch, Emoji, Space & Enter (iOS style layout)
        listOf(
            KeyModel(-2, "123", KeyType.FUNCTION, flexWidth = 1.6f),
            KeyModel(-4, ":)", KeyType.FUNCTION, flexWidth = 1.4f),
            KeyModel(32, "space", KeyType.SPACE, flexWidth = 4.5f),
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
        // Row 4: ABC switch, Emoji, Space & Enter
        listOf(
            KeyModel(-2, "ABC", KeyType.FUNCTION, flexWidth = 1.6f),
            KeyModel(-4, ":)", KeyType.FUNCTION, flexWidth = 1.4f),
            KeyModel(32, "space", KeyType.SPACE, flexWidth = 4.5f),
            KeyModel(10, "return", KeyType.FUNCTION, flexWidth = 2.5f)
        )
    )

    // Text-emoji (kaomoji) panel: each page is 3 rows of 9 keys + the shared
    // bottom control row (ABC / next page / space / return). Built once from
    // TextEmojiData so the curated strings live in one place.
    private val emojiPages: List<List<List<KeyModel>>> = TextEmojiData.pages.map { entries ->
        val contentRows = entries.chunked(9).map { rowEntries ->
            rowEntries.map { text -> KeyModel(KEYCODE_TEXT_EMOJI, text, KeyType.NORMAL, insertText = text) }
        }
        contentRows + listOf(
            listOf(
                KeyModel(-3, "ABC", KeyType.FUNCTION, flexWidth = 1.6f),
                KeyModel(KEYCODE_EMOJI_NEXT_PAGE, "\u203a", KeyType.FUNCTION, flexWidth = 1.4f),
                KeyModel(32, "space", KeyType.SPACE, flexWidth = 4.5f),
                KeyModel(10, "return", KeyType.FUNCTION, flexWidth = 2.5f)
            )
        )
    }

    val rows: List<List<KeyModel>>
        get() = when {
            isEmojiMode -> emojiPages[emojiPage]
            isSymbolMode -> symbolRows
            else -> qwertyRows
        }

    fun openEmojiPanel() {
        emojiReturnsToSymbolMode = isSymbolMode
        isEmojiMode = true
        emojiPage = 0
    }

    fun closeEmojiPanel() {
        isEmojiMode = false
        isSymbolMode = emojiReturnsToSymbolMode
    }

    fun nextEmojiPage() {
        if (emojiPages.isEmpty()) return
        emojiPage = (emojiPage + 1) % emojiPages.size
    }

    fun measure(width: Float, height: Float, horizontalPadding: Float = 6f, verticalPadding: Float = 5f, context: Context? = null) {
        if (width <= 0 || height <= 0) return

        // Read keyboard key size scale factor from preferences (80% to 130%)
        val scaleFactor = if (context != null) {
            val prefs = context.getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE)
            prefs.getInt("keyboard_scale", 100) / 100f
        } else {
            1.0f
        }

        val activeRows = rows
        val rowCount = activeRows.size
        val availableHeight = height - (verticalPadding * (rowCount + 1))
        val baseRowHeight = (availableHeight / rowCount) * 0.90f

        var currentY = verticalPadding + ((availableHeight - (baseRowHeight * rowCount)) / 2f)

        // Standard unit width calculated based on 10 keys (Row 0 & Row 1)
        val standard10KeyTotalFlex = 10f
        val availableWidth = width - (horizontalPadding * 11)
        val baseStandardUnitWidth = availableWidth / standard10KeyTotalFlex

        // The qwerty home-row centering treatment only applies to the qwerty
        // layout itself -- symbol and emoji pages use plain flex-based rows.
        val useHomeRowCentering = !isSymbolMode && !isEmojiMode

        activeRows.forEachIndexed { index, row ->
            if (useHomeRowCentering && index == 2) {
                val rowKeysCount = row.size
                val totalRowKeysWidth = rowKeysCount * baseStandardUnitWidth
                val totalGapsWidth = (rowKeysCount - 1) * horizontalPadding
                val rowTotalWidth = totalRowKeysWidth + totalGapsWidth
                
                val sideMargin = (width - rowTotalWidth) / 2f
                var currentX = sideMargin

                row.forEach { key ->
                    val cellRect = RectF(
                        currentX,
                        currentY,
                        currentX + baseStandardUnitWidth,
                        currentY + baseRowHeight
                    )
                    key.bounds = scaleRect(cellRect, scaleFactor)
                    currentX += baseStandardUnitWidth + horizontalPadding
                }
            } else {
                val totalFlexWidth = row.sumOf { it.flexWidth.toDouble() }.toFloat()
                val rowAvailableWidth = width - (horizontalPadding * (row.size + 1))
                val baseUnitWidth = rowAvailableWidth / totalFlexWidth

                var currentX = horizontalPadding

                row.forEach { key ->
                    val cellWidth = baseUnitWidth * key.flexWidth
                    val cellRect = RectF(
                        currentX,
                        currentY,
                        currentX + cellWidth,
                        currentY + baseRowHeight
                    )
                    key.bounds = scaleRect(cellRect, scaleFactor)
                    currentX += cellWidth + horizontalPadding
                }
            }

            currentY += baseRowHeight + verticalPadding
        }
    }

    private fun scaleRect(rect: RectF, scale: Float): RectF {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val newWidth = rect.width() * scale
        val newHeight = rect.height() * scale
        return RectF(
            cx - newWidth / 2f,
            cy - newHeight / 2f,
            cx + newWidth / 2f,
            cy + newHeight / 2f
        )
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

    fun keyCenter(char: Char): PointF? {
        val target = char.lowercaseChar().toString()
        qwertyRows.forEach { row ->
            row.forEach { key ->
                if (key.label == target && key.bounds.width() > 0f) {
                    return PointF(key.bounds.centerX(), key.bounds.centerY())
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
        const val KEYCODE_EMOJI = -4
        const val KEYCODE_TEXT_EMOJI = -6
        const val KEYCODE_EMOJI_NEXT_PAGE = -7
    }
}
