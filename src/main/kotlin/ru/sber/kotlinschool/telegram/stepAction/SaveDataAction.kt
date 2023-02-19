package ru.sber.kotlinschool.telegram.stepAction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("SAVE_DATE")
class SaveDataAction:ActionExecutor() {

    @Autowired
    private lateinit var userState: UserState

    override fun execute(prevStep: Step, message: String, chatId: String): Step?
    {
        prevStep.configParams.forEach {
            userState.updateTmpMap(chatId, UserParam.valueOf(it),message)
        }
        return null
    }
}