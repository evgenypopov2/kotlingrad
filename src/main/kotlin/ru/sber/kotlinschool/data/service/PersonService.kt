package ru.sber.kotlinschool.data.service

import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.repository.PersonRepository
import java.util.*

@Service
class PersonService(private val personRepository: PersonRepository) {

    fun findAll(): List<Person> = personRepository.findAll()
    fun findAllClients() = personRepository.findAllClients()
    fun findById(id: Long): Optional<Person> = personRepository.findById(id)

    fun save(person: Person): Person = personRepository.save(person)

    fun registerUser(person: Person): Person =
        findById(person.id)
            .map { it }
            .orElse(personRepository.save(person))
}