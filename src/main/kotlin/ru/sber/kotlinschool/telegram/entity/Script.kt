package ru.sber.kotlinschool.telegram.entity

import com.fasterxml.jackson.annotation.JsonProperty

class Script(

    @JsonProperty("startPoint")
    val startPoint:String,

    @JsonProperty("steps")
    val steps:List<Step>)
{
}