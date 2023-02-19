package ru.sber.kotlinschool.data.service

import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import ru.sber.kotlinschool.data.repository.ServiceScheduleRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import kotlin.collections.ArrayList

@Service
class ServiceSchedule(private val scheduleRepository: ServiceScheduleRepository) {

    //private val logger = LoggerFactory.getLogger(ServiceSchedule::class.java)

    fun findServiceScheduleByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        executor: Person
    ): List<ServiceSchedule> {
        return scheduleRepository.findServiceScheduleByDateRange(startDate, endDate, executor)
    }

    fun getWeekSchedule(anchorDate: LocalDate, executor: Person): List<String> {
        val result = ArrayList<String>()
        val schedule = findServiceScheduleByDateRange(
            anchorDate.with(DayOfWeek.MONDAY), LocalDate.now().with(DayOfWeek.SUNDAY), executor)

        for (value in DayOfWeek.values()) {
            val currentDate = anchorDate.with(DayOfWeek.MONDAY).plusDays(value.ordinal.toLong())
            var caption = currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EE"))

            if (schedule.any { it.date == currentDate })
                caption += "*"

            result.add(caption)
        }
        return result
    }

    fun findServiceScheduleByDate(selectedDate: LocalDate, executor: Person) : List<ServiceSchedule> {
        return scheduleRepository.findServiceScheduleByDate(selectedDate, executor)
    }

    fun getDaySchedule(selectedDate: LocalDate
                       , executor: Person
                       , startTime: LocalTime = LocalTime.of(9, 0, 0, 0)
                       , endTime: LocalTime = LocalTime.of(18, 0, 0, 0)
    ): MutableMap<Long, String> {
        val result: MutableMap<Long, String> = HashMap()
        val schedule = findServiceScheduleByDate(selectedDate, executor)

        for (item in schedule) {
            result[item.id] = "${item.time.format(DateTimeFormatter.ofPattern("HH:mm"))}: ${item.customer.fio} - ${item.service.name}"
        }

/*
    -- Вариант с отображением свободных часов... Хотя нафиг они нужны?
        var currentTime = startTime

        while (currentTime <= endTime) {
            val filtered = schedule.filter { it -> it.time >= currentTime && it.time <= currentTime.plus(Duration.ofHours(1).minus(Duration.ofSeconds(1))) }

            // Пока исходим из того, что 1 услуга в час. Остальное на перспективу:
            if (filtered.isNotEmpty())
                result.add("${currentTime.format( DateTimeFormatter.ofPattern("HH:mm"))}: ${filtered[0].customer.fio} - ${filtered[0].service.name}")
            else
                result.add("${currentTime.format( DateTimeFormatter.ofPattern("HH:mm"))}: - <не занят>" )

            currentTime = currentTime.plus(Duration.ofHours(1));
        }
*/
        return result
    }

    fun getScheduleDetailInfo(id: Long): String {
        val row = scheduleRepository.findById(id)

        if (row.isEmpty)
            return "Заказ не найден."
        else {
            val state = if(row.get().isVisited) "Выполнен" else "Новый"

            return "Информация о заказе:\n\n" +
                    "Наименование: ${row.get().service.name}\n" +
                    "Дата: ${row.get().date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}\n" +
                    "Время: ${row.get().time.format(DateTimeFormatter.ofPattern("HH:mm"))}\n" +
                    "Исполнитель: ${row.get().service.executor.fio}\n" +
                    "Стоимость: ${row.get().service.price/100}\n" +
                    "Статус: $state\n\n" +
                    "Клиент:\n\n" +
                    "ФИО: ${row.get().customer.fio}\n" +
                    "Телефон: ${row.get().customer.phone}\n"
        }
    }

    fun getSelectedDate(selectedDate: String?): LocalDate {
        var result = LocalDate.of(3033, 3, 13)

        if (selectedDate.isNullOrEmpty())
            return result

        if (selectedDate.contains(Regex("""\d{2}\s\D{3,8}\s\d{4},\s\D{2}"""))) {

            val formatter = DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ofPattern("dd MMMM yyyy, EE"))
                .toFormatter(Locale("Ru"))

            val parsed = formatter.parse(selectedDate.replace("*", ""))

            result = LocalDate.of(
                parsed.get(ChronoField.YEAR),
                parsed.get(ChronoField.MONTH_OF_YEAR),
                parsed.get(ChronoField.DAY_OF_MONTH)
            )
        }
        return result
    }

}