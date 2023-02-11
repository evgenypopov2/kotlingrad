package ru.sber.kotlinschool.telegram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sber.kotlinschool.telegram.StepType

class Step(

    @JsonProperty("title")
    val title:String,

    @JsonProperty("messageForUser")
    val messageForUser:String,

    @JsonProperty("nextSteps")
    val nextSteps:List<String>,

    @JsonProperty("type")
    val type: StepType)
{
}