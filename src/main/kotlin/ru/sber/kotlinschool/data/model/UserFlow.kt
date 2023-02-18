package ru.sber.kotlinschool.data.model

data class UserFlow(
    /*
    key - название шага, value - набор выбранных параметров
     */
    val steps: MutableMap<String, String>  = LinkedHashMap()
)