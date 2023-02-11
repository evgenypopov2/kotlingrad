package ru.sber.kotlinschool.telegram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sber.kotlinschool.telegram.ActionType

class Step(

    @JsonProperty("id")
    val id: Int,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("messageForUser")
    val messageForUser: String,

    @JsonProperty("nextStepId")
    val nextStepId: List<Int>,

    @JsonProperty("actionOnStep")
    val actionOnStep: ActionType
)
{
    var parent: Step? = null
    lateinit var children: List<Step>
}