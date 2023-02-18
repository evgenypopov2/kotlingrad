package ru.sber.kotlinschool.telegram.const

import com.vdurmont.emoji.EmojiParser;

enum class Icon(val value: String) {
    TIME(":clock4:"),
    CLIENTS(":busts_in_silhouette:"),
    MONEY(":moneybag:"),
    EXIT(":exit:");

    fun value(): String? {
        return EmojiParser.parseToUnicode(value)
    }
}