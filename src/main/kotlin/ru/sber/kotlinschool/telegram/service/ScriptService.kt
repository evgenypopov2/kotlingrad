package ru.sber.kotlinschool.telegram.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.telegram.entity.Script
import ru.sber.kotlinschool.telegram.entity.Step

@Service
class ScriptService {

    private val mapper: ObjectMapper = ObjectMapper();

    val adminScript = getScript("adminScript.json")

    val timeRegex = Regex("[0-9]{2}:[0-9]{2}")

//    val clientScript = getScript("clientScript.json")

    private fun getScript(scriptName:String): Script
    {
        val fileContent = this::class.java.classLoader.getResource(scriptName).readText()
        return mapper.readValue(fileContent)
    }

    fun getCurrentStep(message: String): Step
    {
        if(message.contains(timeRegex))
           return adminScript.steps.single { it.title.contains("TIME_STEP") };

        return adminScript.steps.single { it.title.split("|").contains(message) };
    }
}