package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.service.UserState

@Component("EDIT_MSG")
class EditMessageBuilder : StepBuilder()
{
    @Autowired
    private lateinit var userState: UserState

    override fun build(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)

        val removeKeyBoard = ReplyKeyboardRemove()
        removeKeyBoard.removeKeyboard = true
        responseMessage.replyMarkup = removeKeyBoard
        return responseMessage
    }
}