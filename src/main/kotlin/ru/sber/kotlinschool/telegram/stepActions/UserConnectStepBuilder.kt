package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step

@Component("CONNECT_WITH_USER")
class UserConnectStepBuilder : StepBuilder(){
    override fun build(currentStep: Step, chatId: String): SendMessage {
        TODO("Not yet implemented")
    }
}