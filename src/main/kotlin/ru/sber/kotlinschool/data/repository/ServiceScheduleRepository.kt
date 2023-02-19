package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import java.time.LocalDate

@Repository
interface ServiceScheduleRepository: JpaRepository<ServiceSchedule, Long> {

    @Query("select ss from ServiceSchedule ss inner join ss.service sp where ss.date between :start_date and :end_date and sp.executor = :executor order by ss.date, ss.time")
    fun findServiceScheduleByDateRange(@Param("start_date") startDate: LocalDate, @Param("end_date") endDate: LocalDate, @Param("executor") executor: Person): List<ServiceSchedule>

    @Query("select ss from ServiceSchedule ss inner join ss.service sp where ss.date = :selected_date and sp.executor = :executor order by ss.date, ss.time")
    fun findServiceScheduleByDate(@Param("selected_date") selectedDate: LocalDate, @Param("executor") executor: Person): List<ServiceSchedule>
}