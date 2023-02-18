package ru.sber.kotlinschool.data.entity

import javax.persistence.*

@Entity
data class Person(
    @Id
    @Column(name = "telegram_id", unique = true)
    val id: Long? = null,
    @Column(nullable = false)
    val fio: String,
    val phone: String,
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    val role: PersonRole = PersonRole.CLIENT,
    @Column(name = "first_name")
    val firstName: String,
    @OneToMany(
        mappedBy = "customer",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val customerSchedules: MutableList<ServiceSchedule> = mutableListOf(),
    @OneToMany(
        mappedBy = "executor",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val executorSchedules: MutableList<ServiceSchedule> = mutableListOf()
)
