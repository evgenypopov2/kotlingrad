package ru.sber.kotlinschool.data.entity

import java.util.Date
import javax.persistence.*

@Entity
data class ServiceSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val service: ServiceProvided,
    val date: Date,
    val time: Int
)
