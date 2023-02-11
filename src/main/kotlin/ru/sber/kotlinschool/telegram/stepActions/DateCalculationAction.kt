package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step

@Component("DATE_CALC")
class DateCalculationAction: Action() {

    override fun execute(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)
        val range = toRange(currentStep.nextSteps[0])
        val list = mutableListOf<List<String>>()
        for(i in range)
        {
           list.add(listOf("${i}:00"))
        }

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    fun toRange(str: String): IntRange = str
        .split("-")
        .let { (a, b) -> a.toInt()..b.toInt() }
}