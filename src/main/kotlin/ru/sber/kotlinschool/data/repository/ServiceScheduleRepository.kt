package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import java.time.LocalDate

@Repository
interface ServiceScheduleRepository: JpaRepository<ServiceSchedule, Long> {

    @Query("select ss from ServiceSchedule ss where ss.date = :dt")
    fun findServiceScheduleByDate(@Param("dt") date: LocalDate): List<ServiceSchedule>

}