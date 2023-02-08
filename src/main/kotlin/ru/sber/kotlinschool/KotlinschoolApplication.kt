package ru.sber.kotlinschool

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinschoolApplication

fun main(args: Array<String>) {
	runApplication<KotlinschoolApplication>(*args)
}
