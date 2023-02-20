package ru.sber.kotlinschool.data.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.ServiceRegistration
import ru.sber.kotlinschool.data.repository.ServiceRegistrationRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

@Service
class ServiceRegistrationService(
    private val serviceRegistrationRepository: ServiceRegistrationRepository,
    private val serviceProvidedService: ServiceProvidedService

) {
    private val FORMATTER = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ofPattern("dd MMMM, EE"))
        .toFormatter(Locale("Ru"))

    @Value("\${admin.startWorkingHour}")
    private var startHour: Int = 10

    @Value("\${admin.endWorkingHour}")
    private var endHour: Int = 19

    fun findServiceScheduleByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        executor: Person
    ): List<ServiceRegistration> {
        return serviceRegistrationRepository.findServiceRegistrationByDateRange(startDate, endDate, executor)
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

    fun findServiceScheduleByDate(selectedDate: LocalDate, executor: Person) : List<ServiceRegistration> {
        return serviceRegistrationRepository.findServiceRegistrationByDate(selectedDate, executor.id)
    }

    fun getDaySchedule(selectedDate: LocalDate
                       , executor: Person
                       , startTime: LocalTime = LocalTime.of(9, 0, 0, 0)
                       , endTime: LocalTime = LocalTime.of(18, 0, 0, 0)
    ): MutableMap<Long, String> {
        val result: MutableMap<Long, String> = HashMap()
        val schedule = findServiceScheduleByDate(selectedDate, executor)

        for (item in schedule) {
            result[item.id] = "${item.time.format(DateTimeFormatter.ofPattern("HH:mm"))}: ${item.client.fio} - ${item.service.name}"
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
        val row = serviceRegistrationRepository.findById(id)

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
                    "ФИО: ${row.get().client.fio}\n" +
                    "Телефон: ${row.get().client.phone}\n"
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

    fun findServiceScheduleByDate(date: LocalDate) = serviceRegistrationRepository.findServiceScheduleByDate(date)
    fun getUserShedule(id: Long, date: LocalDate): List<String> {
        val result: ArrayList<String> = ArrayList()
        for (srvReg in getRegistrationsByUserAndDate(id, date)) {// 10-00 Семенов Семен, стрижка, цена
            result.add("${srvReg.time} ${srvReg.client.fio} ${srvReg.service.name} ${srvReg.service.price / 100}")
        }
        return result
    }
    fun getRegistrationsByUserAndDate(userId: Long, date: LocalDate): List<ServiceRegistration> {
        return serviceRegistrationRepository.getRegistrationsByUserAndDate(userId, date)
    }

    /**
     * Генерирует расписание на неделю вперед
     * key - дата, value - список незанятых часов
     */
    fun getFreeDays(serviceProvided: String): MutableMap<String, MutableList<Int>> {
        val serviceProvidedId = serviceProvidedService.getIdByName(serviceProvided)
        val allRecords =
            serviceRegistrationRepository.findAll().filter {
                it.executor != null
                        && it.service.id == serviceProvidedId
                        && it.date.isAfter(LocalDate.now().minusDays(1))
            }
                .toCollection(ArrayList())
        val freeDaysHours = getScheduleTemplate()
        // из расписания с текущего дня удалить занятые часы
        allRecords.forEach { record ->
            freeDaysHours[record.date.format(FORMATTER)]?.also { hours ->
                hours.remove(record.time.hour)
            }
        }

        val itr = freeDaysHours.iterator()
        while (itr.hasNext()) {
            if (itr.next().value.isEmpty()) {
                itr.remove()
            }
        }

        return freeDaysHours
    }

    fun getScheduleTemplate(): MutableMap<String, MutableList<Int>> {
        var date = LocalDate.now()
        val firstKey = date.format(FORMATTER)
        val scheduleTemplate = mutableMapOf<String, MutableList<Int>>()
        val hours = mutableListOf<Int>()
        for (i in startHour until endHour) {
            hours.add(i)
        }
        for (i in 0..6) {
            val dateForButton = date.format(FORMATTER)
            scheduleTemplate.set(dateForButton, hours.toMutableList())
            date = date.plusDays(1)
        }
        val time = LocalTime.now().hour.plus(1)
        var todayStartHour = startHour
        if (time in startHour until endHour) {
            todayStartHour = time
        }
        val todayHours = mutableListOf<Int>()
        for (i in todayStartHour until endHour) {
            todayHours.add(i)
        }
        scheduleTemplate.set(firstKey, todayHours)

        return scheduleTemplate
    }

    fun save(record: ServiceRegistration) = serviceRegistrationRepository.save(record)

}