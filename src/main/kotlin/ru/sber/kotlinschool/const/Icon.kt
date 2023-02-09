package ru.sber.kotlinschool.const

import com.vdurmont.emoji.EmojiParser;

enum class Icon(val value: String) {
    TIME(":clock4:"),
    CLIENTS(":busts_in_silhouette:"),
    MONEY(":moneybag:");

    fun value(): String? {
        return EmojiParser.parseToUnicode(value)
    }
}