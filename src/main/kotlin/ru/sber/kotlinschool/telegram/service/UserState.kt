package ru.sber.kotlinschool.telegram.service

import org.springframework.stereotype.Component
import ru.sber.kotlinschool.telegram.entity.UserParam
import java.util.concurrent.ConcurrentHashMap

@Component
class UserState {

    private val stepHistory: ConcurrentHashMap<String, State> = ConcurrentHashMap()

    fun setCurrentStep(clientId:String, state:State)
    {
        stepHistory[clientId]?.tmpParam?.let { state.tmpParam.putAll(it) }

        stepHistory[clientId] = state
    }

    fun getPrevStep(clientId:String): State?
    {
       return stepHistory[clientId]
    }

    fun updateTmpMap(chatId: String, param: UserParam, data: String) {
        stepHistory[chatId]?.tmpParam?.set(param, data)
    }

    fun getTmpParam(chatId: String, param: UserParam):String?
    {
        return stepHistory[chatId]?.tmpParam?.get(param)
    }

    fun cleanHistory(chatId: String)
    {
        stepHistory.remove(chatId)
    }
}

class State(val stepId: Int, val sendMessageText:String, val tmpParam: MutableMap<UserParam, String> = mutableMapOf()) {}