package ru.sber.kotlinschool.telegram.service

import ru.sber.kotlinschool.data.entity.ServiceSchedule
import ru.sber.kotlinschool.data.entity.ServiceScheduleList
import java.time.LocalDate

interface ScheduleService {
    fun findServiceScheduleByDate(date: LocalDate): List<ServiceSchedule>

    fun getSheduleByUserAndDate(userId: Long, date: LocalDate): List<ServiceScheduleList>
}