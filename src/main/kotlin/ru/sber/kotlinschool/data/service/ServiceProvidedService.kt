package ru.sber.kotlinschool.data.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.data.repository.ServiceProvidedRepository

@Service
class ServiceProvidedService(@Autowired val repository: ServiceProvidedRepository) {

    fun getServiceProvidedNames() = repository.findAll().map { it.name }.toCollection(ArrayList())

    fun getServiceProvidedList() = repository.findAll()

    fun getIdByName(serviceProvidedName: String) = repository.findByName(serviceProvidedName).id

    fun getByName(serviceProvidedName: String) = repository.findByName(serviceProvidedName)
}