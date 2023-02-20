package ru.sber.kotlinschool.data.entity

import javax.persistence.*

@Entity
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val fio: String,

    val phone: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    val role: PersonRole = PersonRole.CLIENT,

    @Column(name = "first_name")
    val firstName: String
) {
    override fun toString(): String {
        return "id = $id; fio = $fio; phone = $phone; role = $role; first_name = $firstName"
    }
}
