package ru.sber.kotlinschool.data.entity

import javax.persistence.*

@Entity
data class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val fio: String,
    val phone: String,
    val tLogin: String,
    @Enumerated(value = EnumType.STRING)
    val role: PersonRole
)
