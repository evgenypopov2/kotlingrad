package ru.sber.kotlinschool.telegram.service

import org.springframework.stereotype.Component

@Component
class UserState {

    private val userStates: MutableMap<String, String> = mutableMapOf<String, String>()
    private val userStates2: MutableMap<String, Int> = mutableMapOf<String, Int>()

//    fun setState(clientId:String, message:String)
//    {
//        userStates.put(clientId, message)
//    }

    fun setState(clientId:String, message:Int)
    {
        userStates2.put(clientId, message)
    }

    fun getState(clientId:String): Int?
    {
       return userStates2[clientId]
    }

}