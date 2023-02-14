package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.ServiceScheduleList
import java.time.LocalDate

@Repository
interface ServiceSheduleResultRepository : JpaRepository<ServiceScheduleList, Long>  {
    @Query("select ROW_NUMBER() OVER () as id, to_char(ss.service_time, 'hh:mi') as time, p.fio , sp.name, sp.price\n" +
            "from service_schedule ss\n" +
            "         inner join service_provided sp on ss.service_id = sp.id\n" +
            "         inner join person p on p.telegram_id = ss.customer_telegram_id\n" +
            "where ss.service_date = :dt\n" +
            "    and sp.executor_telegram_id = :user_id\n" +
            "order by ss.service_time;", nativeQuery = true)
    fun getSheduleByUserAndDate(@Param("user_id") userId: Long, @Param("dt") date: LocalDate): List<ServiceScheduleList>

}