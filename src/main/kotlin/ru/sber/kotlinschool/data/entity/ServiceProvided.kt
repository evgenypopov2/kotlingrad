package ru.sber.kotlinschool.data.entity

import javax.persistence.*

@Entity
@Table
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
    @OneToMany(mappedBy = "", cascade = [CascadeType.ALL])
    val serviceProvidedSchedules: MutableList<ServiceSchedule> = mutableListOf()
)