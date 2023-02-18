package ru.sber.kotlinschool.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sber.kotlinschool.data.entity.ServiceProvided

@Repository
interface ServiceProvidedRepository: JpaRepository<ServiceProvided, Long> {
    @Query("select sp from ServiceProvided  sp where sp.name = :name")
   fun findByName(@Param("name") serviceProvidedName: String) : ServiceProvided
}