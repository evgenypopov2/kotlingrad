package ru.sber.kotlinschool.telegram.stepAction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("CHANGE_MSG")
class ChangeMessageAction:ActionExecutor() {

    @Autowired
    private lateinit var userState: UserState

    override fun execute(prevStep: Step, message: String, chatId: String): Step?
    {
        userState.updateTmpMap(chatId, UserParam.MESSAGE_FOR_USER,message)
        return prevStep.children[0]
    }
}