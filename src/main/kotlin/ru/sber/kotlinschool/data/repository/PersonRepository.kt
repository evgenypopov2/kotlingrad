package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.Person

@Repository
interface PersonRepository: JpaRepository<Person, Long> {
    @Query("select p from Person p where p.role = 'CLIENT'")
    fun findAllClients() : List<Person>
}