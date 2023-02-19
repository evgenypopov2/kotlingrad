package ru.sber.kotlinschool.telegram.entity

import com.fasterxml.jackson.annotation.JsonProperty

class Step(

    @JsonProperty("id")
    val id: Int,                        //id шага

    @JsonProperty("title")
    val title: String,                  //заголовок, который будет отображен на кнопке у шага-родителя

    @JsonProperty("messageForUser")
    val messageForUser: String,         //сообщение, которое будет выведено пользователю при выборе шага

    @JsonProperty("nextStepId")
    val nextStepId: List<Int>,          //список следующих шагов

    @JsonProperty("stepType")
    val stepType: StepType,             //тип шага, на основе данного типа формируется различное отображение

    @JsonProperty("executeAction")
    val executeAction: String?,       //событие, которое должно быть выполнено после взаимодейтсвия с шагом

    @JsonProperty("configParams")
    val configParams: List<String>      //параметры, которые необходимы для построения шага
)
{
    var parent: Step? = null
    lateinit var children: List<Step>
}