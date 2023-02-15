package ru.sber.kotlinschool.telegram.entity

import com.fasterxml.jackson.annotation.JsonProperty

class Step(

    @JsonProperty("id")
    val id: Int,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("messageForUser")
    val messageForUser: String,

    @JsonProperty("nextStepId")
    val nextStepId: List<Int>,

    @JsonProperty("stepType")
    val stepType: StepType,

    @JsonProperty("configParams")
    val configParams: List<String>
)
{
    var parent: Step? = null
    lateinit var children: List<Step>
}