package ru.sber.kotlinschool.data.entity

import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class ServiceRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val date: Date,
    @ManyToOne
    val service: ServiceProvided,
    @ManyToOne
    val client: Person,
    val visited: Boolean
)
