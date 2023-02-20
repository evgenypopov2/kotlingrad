package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.ServiceRegistration
import java.time.LocalDate

@Repository
interface ServiceRegistrationRepository: JpaRepository<ServiceRegistration, Long> {
    @Query("select ss from ServiceRegistration ss where ss.date between :start_date and :end_date and ss.executor = :executor order by ss.date, ss.time")
    fun findServiceRegistrationByDateRange(@Param("start_date") startDate: LocalDate, @Param("end_date") endDate: LocalDate, @Param("executor") executor: Person): List<ServiceRegistration>

    @Query("select sr from ServiceRegistration sr where sr.date = ?1 and sr.executor.id = ?2 order by sr.date, sr.time")
    fun findServiceRegistrationByDate(date: LocalDate, executorId: Long): List<ServiceRegistration>


    @Query("select ss from ServiceRegistration ss where ss.date = :dt")
    fun findServiceScheduleByDate(@Param("dt") date: LocalDate): List<ServiceRegistration>

    @Query("select sr from ServiceRegistration sr where sr.service = :service and sr.executor is not null")
    fun getBusyDays(@Param("service") service: Long): List<ServiceRegistration>


    @Query("select sr from ServiceRegistration sr where sr.date = :dt and sr.client.id = :userId")
    fun getRegistrationsByUserAndDate(@Param("userId") userId: Long, @Param("dt") date: LocalDate): List<ServiceRegistration>

}