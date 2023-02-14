package ru.sber.kotlinschool.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utils {

    fun getButtonsList(arr: List<String>) : List<List<String>> {
        val result : MutableList<ArrayList<String>> = mutableListOf()
        for (value in arr) {
            val tmp = ArrayList<String>()
            tmp.add(value)
            result.add(tmp)
        }
        return result
    }

    fun getWeeksDates() : ArrayList<String> {
        val result = ArrayList<String>()
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
        //
        val pattern = "dd MMMM yyyy, EE"

        for (value in DayOfWeek.values()) {
            result.add(weekStart.plusDays(value.ordinal.toLong()).format(DateTimeFormatter.ofPattern(pattern)))
        }
        return result
    }

}