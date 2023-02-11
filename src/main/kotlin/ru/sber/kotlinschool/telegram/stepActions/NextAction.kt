package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step

@Component("NEXT")
class NextAction: Action()
{
    override fun execute(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)
        val list = currentStep.nextSteps.map { nextStep -> listOf(nextStep)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }
}