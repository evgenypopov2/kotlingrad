package ru.sber.kotlinschool.data.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table(uniqueConstraints = arrayOf(
    UniqueConstraint(name = "schedule_un", columnNames = arrayOf("customer_telegram_id", "service_id", "service_date", "service_time"))
))
class ServiceSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "ins_date", nullable = false, columnDefinition = "timestamp with time zone DEFAULT CURRENT_TIMESTAMP")
    val insDate: LocalDateTime = LocalDateTime.now(),
    @OneToOne(cascade = [CascadeType.ALL])
    val customer: Person,
    @OneToOne(cascade = [CascadeType.ALL])
    val service: ServiceProvided,
    @Column(name = "service_date", nullable = false, columnDefinition = "date")
    val date: LocalDate,
    @Column(name = "service_time", nullable = false)
    val time: LocalTime,
    @Column(name = "is_visited", nullable = false, columnDefinition = "boolean DEFAULT false")
    val isVisited: Boolean
)
