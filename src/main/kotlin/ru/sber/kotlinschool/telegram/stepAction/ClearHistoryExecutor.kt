package ru.sber.kotlinschool.telegram.stepAction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.service.UserState

@Component("CLEAR_HISTORY")
class ClearHistoryExecutor:ActionExecutor() {

    @Autowired
    private lateinit var userState: UserState

    override fun execute(prevStep: Step, message: String, chatId: String): Step?
    {
        userState.cleanHistory(chatId)
        return null
    }
}