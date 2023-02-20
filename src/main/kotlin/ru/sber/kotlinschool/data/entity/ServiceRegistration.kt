package ru.sber.kotlinschool.data.entity

import java.time.LocalDate
import java.time.LocalTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.random.Random

@Entity
data class ServiceRegistration(
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Random.nextLong(),
    val date: LocalDate,
    val time: LocalTime,
    @ManyToOne
    @JoinColumn(name = "service_id")
    val service: ServiceProvided,
    @ManyToOne
    @JoinColumn(name = "client_id")
    val client: Person,
    @ManyToOne
    @JoinColumn(name = "executor_id")
    val executor: Person,
    val isVisited: Boolean
)
