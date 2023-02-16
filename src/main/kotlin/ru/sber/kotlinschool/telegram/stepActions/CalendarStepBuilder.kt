package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlinschool.telegram.entity.Step


@Component("CREATE_CALENDAR")
class CalendarStepBuilder : StepBuilder() {

    override fun build(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(false)

        val inlineKeyboardMarkup = InlineKeyboardMarkup() //Создаем объект разметки клавиатуры
        val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList() //Создаём ряд

        val weekdays = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница")

        weekdays.forEach {
            val inlineKeyboardButton1 = InlineKeyboardButton()
            inlineKeyboardButton1.text = it
            inlineKeyboardButton1.callbackData = it
            keyboardButtonsRow1.add(inlineKeyboardButton1)
        }

        rowList.add(keyboardButtonsRow1)

        inlineKeyboardMarkup.keyboard = rowList

        responseMessage.replyMarkup = inlineKeyboardMarkup

        return responseMessage
    }
}