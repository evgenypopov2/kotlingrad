package ru.sber.kotlinschool.telegram.service

import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import ru.sber.kotlinschool.data.entity.ServiceScheduleList
import ru.sber.kotlinschool.data.repository.ServiceScheduleRepository
import ru.sber.kotlinschool.data.repository.ServiceSheduleResultRepository
import java.time.LocalDate

@Service
class ScheduleServiceImpl(private val scheduleRepository: ServiceScheduleRepository,
                            private val sheduleResultRepository: ServiceSheduleResultRepository
                        ): ScheduleService {

    override fun findServiceScheduleByDate(date: LocalDate): List<ServiceSchedule> {
        return scheduleRepository.findServiceScheduleByDate(date)
    }

    override fun getSheduleByUserAndDate(userId: Long, date: LocalDate): List<ServiceScheduleList> {
        return sheduleResultRepository.getSheduleByUserAndDate(userId, date)
    }

    fun getUserShedule(id: Long, date: LocalDate): List<String> {
        val result: ArrayList<String> = ArrayList()
        for (service in getSheduleByUserAndDate(id, date)) {
            // 10-00 Семенов Семен, стрижка, цена
            result.add("${service.time} ${service.fio} ${service.name} ${service.price/100}")
        }
        return result
    }

}