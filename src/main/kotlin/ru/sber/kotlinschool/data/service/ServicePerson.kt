package ru.sber.kotlinschool.data.service

import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.repository.PersonRepository
import java.util.*

@Service
class ServicePerson(private val personRepository: PersonRepository) {

    fun findPersonById(id: Long): Optional<Person> {
        return personRepository.findById(id)
    }

    fun registryUser(person: Person): Person {
        var result = findPersonById(person.id)
        if (result.isEmpty) {
           personRepository.save(person)
            return person
        } else
            return result.get()
    }
}