package ru.sber.kotlinschool.data.entity

import java.time.LocalTime
import javax.persistence.*


//https://sysout.ru/sqlresultsetmapping-prevrashhenie-rezultata-sql-zaprosa-v-obekt/

@Entity
data class ServiceScheduleList(
    @Id
    val id: Long,
    val time: LocalTime,
    val fio: String,
    val name: String,
    val price: Long
)