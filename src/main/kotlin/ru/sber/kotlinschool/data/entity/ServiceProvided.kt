package ru.sber.kotlinschool.data.entity

import javax.persistence.*

@Entity
@Table(uniqueConstraints = arrayOf(
    UniqueConstraint(name = "service_name_un", columnNames = arrayOf("name", "executor_telegram_id"))
))
data class ServiceProvided(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false, columnDefinition = "integer default 60")
    val duration: Int = 60,
    @Column(nullable = false, columnDefinition = "integer default 1")
    var capacity: Int = 1,
    @Column(nullable = false, columnDefinition = "bigint default 0")
    val price: Int = 0,
    @OneToOne(cascade = [CascadeType.ALL])
    val executor: Person
)