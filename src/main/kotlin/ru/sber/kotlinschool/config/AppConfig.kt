package ru.sber.kotlinschool.config

import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource


@Configuration
class AppConfig {
    @Bean
    fun propertiesFileMapping(): PropertiesFactoryBean {
        val factoryBean = PropertiesFactoryBean()
        factoryBean.setFileEncoding("UTF-8")
        factoryBean.setLocation(ClassPathResource("application.properties"))
        return factoryBean
    }
}