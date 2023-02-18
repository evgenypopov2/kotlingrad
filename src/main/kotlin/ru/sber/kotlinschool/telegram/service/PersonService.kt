package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.repository.PersonRepository

@Service
class PersonService( @Autowired val personRepository: PersonRepository) {

}