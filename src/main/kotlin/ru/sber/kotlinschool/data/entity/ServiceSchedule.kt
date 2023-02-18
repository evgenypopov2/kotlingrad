package ru.sber.kotlinschool.data.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table
data class ServiceSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(
        name = "ins_date",
        nullable = false,
        columnDefinition = "timestamp with time zone DEFAULT CURRENT_TIMESTAMP"
    )
    val insDate: LocalDateTime = LocalDateTime.now(),
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    val customer: Person,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    val service: ServiceProvided,
    @Column(name = "service_date", nullable = false, columnDefinition = "date")
    val date: LocalDate,
    @Column(name = "service_time", nullable = false)
    val time: LocalTime,
    @Column(name = "is_visited", nullable = false, columnDefinition = "boolean DEFAULT false")
    val isVisited: Boolean,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executor_id")
    val executor: Person
)
