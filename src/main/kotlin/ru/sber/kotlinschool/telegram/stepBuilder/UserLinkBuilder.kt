package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step

@Component("USER_TGM_LINK")
class UserLinkBuilder : StepBuilder() {
    override fun build(currentStep: Step, chatId: String): SendMessage {
        val message =
            "<a href=\"https://t.me/Zaynulina_Guzal\">Ссылка на чат пользователя</a>"; //TODO брать из сохраненной ранее информации
        val responseMessage = SendMessage(chatId, message)
        responseMessage.parseMode = ParseMode.HTML
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title) }

        responseMessage.replyMarkup = getReplyMarkup(list)
        return responseMessage
    }
}