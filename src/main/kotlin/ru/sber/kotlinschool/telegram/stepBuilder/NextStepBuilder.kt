package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step

@Component("NEXT")
class NextStepBuilder: StepBuilder()
{
    override fun build(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }
}