package ru.sber.kotlinschool.telegram.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import ru.sber.kotlinschool.telegram.entity.Script
import ru.sber.kotlinschool.telegram.entity.Step

@Service
class ScriptService {

    private val mapper: ObjectMapper = ObjectMapper();

    val adminScript = prepareScript("adminScript.json")

    val timeRegex = Regex("[0-9]{1,2}:[0-9]{2}")

//    val clientScript = getScript("clientScript.json")

    private fun prepareScript(scriptName:String): Script
    {
        val fileContent = this::class.java.classLoader.getResource(scriptName).readText()
        var script = mapper.readValue<Script>(fileContent)
        val stepMap = script.steps.associateBy { it.id }
        script.steps.forEach { step ->
            run {
                step.children = step.nextStepId.map { id -> stepMap[id]!! }
            }
        }
        return script
    }

    fun getCurrentStep(message: String): Step?
    {
        if(message.contains(timeRegex))
           return adminScript.steps.single { it.title.contains("DATE_TIME_CALC") };

        return adminScript.steps.singleOrNull() { it.title.split("|").contains(message) };
    }

    fun getFirstStep(): Step
    {
        return getCurrentStep("/start")!!
    }
}