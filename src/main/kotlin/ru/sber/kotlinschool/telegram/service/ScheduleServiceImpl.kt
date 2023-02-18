package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.ServiceProvided
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import ru.sber.kotlinschool.data.entity.ServiceScheduleList
import ru.sber.kotlinschool.data.repository.ServiceScheduleRepository
import ru.sber.kotlinschool.data.repository.ServiceSheduleResultRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ServiceScheduleRepository,
    private val scheduleResultRepository: ServiceSheduleResultRepository,
    private val serviceProvidedService: ServiceProvidedService
) : ScheduleService {

    private val FORMATTER = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ofPattern("dd MMMM, EE"))
        .toFormatter(Locale("Ru"))

    @Value("\${working_hours.start}")
    private var startHour: Int = 10

    @Value("\${working_hours.end}")
    private var endHour: Int = 19

    override fun findServiceScheduleByDate(date: LocalDate): List<ServiceSchedule> {
        return scheduleRepository.findServiceScheduleByDate(date)
    }

    override fun getSheduleByUserAndDate(userId: Long, date: LocalDate): List<ServiceScheduleList> {
        return scheduleResultRepository.getSheduleByUserAndDate(userId, date)
    }

    fun getUserShedule(id: Long, date: LocalDate): List<String> {
        val result: ArrayList<String> = ArrayList()
        for (service in getSheduleByUserAndDate(id, date)) {// 10-00 Семенов Семен, стрижка, цена
            result.add("${service.time} ${service.fio} ${service.name} ${service.price / 100}")
        }
        return result
    }

    /**
     * Генерирует расписание на неделю вперед
     * key - дата, value - список незанятых часов
     */
    fun getFreeDays(serviceProvided: String): MutableMap<String, MutableList<Int>> {
        val serviceProvidedId = serviceProvidedService.getIdByName(serviceProvided)
        val allRecords =
            scheduleRepository.findAll().filter {
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

    fun getBusyDays(executorId: Long): MutableMap<String, MutableList<Int>> {
        val allRecords =
            scheduleRepository.findAll().filter {
                it.executor.id == executorId
                        && !it.isVisited
                        && it.date.isAfter(LocalDate.now().minusDays(1))
            }
                .toCollection(ArrayList())

        val executorSchedule: MutableMap<String, MutableList<Int>> =
            allRecords.groupBy { it.date.format(FORMATTER) }.mapValues {
                it.value.stream().map { it.time.hour }.collect(
                    Collectors.toList()
                )
            }.toMutableMap()

        return executorSchedule
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

    fun saveRecord(record: ServiceSchedule) {
        scheduleRepository.save(record)
    }

    fun getRevenueByFinishedSchedule(userId: Long): Long {
        val finishedSchedule = scheduleRepository.getFinishedSchedule(userId)
        val priceList: Map<String, Int> = serviceProvidedService.getServiceProvidedList().stream()
            .collect(Collectors.toMap(ServiceProvided::name, ServiceProvided::price))
        var revenue = 0L
        finishedSchedule.forEach { revenue += priceList[it.service.name]!! }

        return revenue
    }

    fun getMySchedule() = scheduleRepository.findAll()
}