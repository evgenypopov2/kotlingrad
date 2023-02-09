package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.ServiceRegistration

@Repository
interface ServiceRegistrationRepository: JpaRepository<ServiceRegistration, Long>