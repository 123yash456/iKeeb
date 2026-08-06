package com.ambhureyr.itypekeyboard.engine.model

/**
 * Curated set of plain-text emoticons and kaomoji, used instead of Unicode
 * color emoji. Everything here is ordinary text (Latin punctuation plus a
 * handful of BMP symbols) that renders consistently across apps and themes,
 * rather than relying on the system's color emoji font.
 *
 * Organized into pages of 27 entries (3 rows x 9) to match the keyboard's
 * row layout, grouped loosely by mood so nearby pages read as sensible sets.
 */
object TextEmojiData {

    private val page1Classic = listOf(
        ":)", ":(", ":D", ":P", ";)", ":'(", ":O", "8)", "xD",
        "-_-", "^_^", ">_<", "T_T", "o_O", ":/", "<3", "</3", ":|",
        ":3", ":*", "X(", "^_~", ":')", "B)", ":$", "\\m/", ":-*"
    )

    private val page2Kaomoji = listOf(
        "(^_^)", "(◕‿◕)", "(¬‿¬)", "(｡◕‿◕｡)", "(￣▽￣)", "(⊙_⊙)", "(°□°)", "(>_<)", "(^_-)",
        "ʕ•ᴥ•ʔ", "(=^･ω･^=)", "(ᵔᴥᵔ)", "( ⚆_⚆)", "(｡♥‿♥｡)", "(╥﹏╥)", "(ㆆ_ㆆ)", "(¬_¬)", "(ノಠ益ಠ)ノ",
        "ヽ(・∀・)ノ", "٩(◕‿◕)۶", "٩(｡•́‿•̀｡)۶", "(づ￣³￣)づ", "(∩˃o˂∩)", "щ(゚Д゚щ)", "( ˘ ³˘)", "o(≧▽≦)o", "(っ˘ω˘ς)"
    )

    private val page3Actions = listOf(
        "¯\\_(ツ)_/¯", "(╯°□°)╯", "( ͡° ͜ʖ ͡°)", "ᕕ(ᐛ)ᕗ", "(ง'̀-'́)ง", "凸(-_-)凸", "\\(≧▽≦)/", "♪(´▽`)", "(⌐■_■)",
        "(∩｀-´)⊃", "(ノ°ο°)ノ", "ヾ(＾∇＾)ﾉ", "(๑˃ᴗ˂)ﻭ", "(~˘▾˘)~", "(づ｡◕‿‿◕｡)づ", "☜(ﾟヮﾟ☜)", "(ノ￣ω￣)ノ", "(¬､¬)",
        "ᕦ(ò_óˇ)ᕤ", "(灬ºωº灬)", "(∪ ◡ ∪)", "( •_•)>⌐■-■", "(๑>ᴗ<๑)", "٩(˘◕‿◕˘)۶", "(⊃｡•́‿•̀｡)⊃", "(ノ_<)", "(*≧ω≦*)"
    )

    val pages: List<List<String>> = listOf(page1Classic, page2Kaomoji, page3Actions)
}
