package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlinschool.telegram.entity.Step

@Component("DATE_TIME_CALC")
class DateTimeCalculationStepBuilder : StepBuilder() {

    @Value("\${admin.startWorkingHour}")
    private val  start: String = ""

    @Value("\${admin.endWorkingHour}")
    private val  end: String = ""

    override fun build(currentStep: Step, chatId: String): SendMessage {

        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)

        val range = start.toInt()..end.toInt()

        val inlineKeyboardMarkup = InlineKeyboardMarkup() //Создаем объект разметки клавиатуры
        val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
        val keyboardButtonsRow2: MutableList<InlineKeyboardButton> = ArrayList()
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList() //Создаём ряд
        val middleNumber = range.first + (range.last-range.first)/2
        for (i in range) {
            val inlineKeyboardButton1 = InlineKeyboardButton()
            inlineKeyboardButton1.text = "${i}:00"
            inlineKeyboardButton1.callbackData = "${i}:00"
            if(i <= middleNumber) {
                keyboardButtonsRow1.add(inlineKeyboardButton1)
            } else {
                keyboardButtonsRow2.add(inlineKeyboardButton1)
            }
        }

        rowList.add(keyboardButtonsRow1)
        rowList.add(keyboardButtonsRow2)

        inlineKeyboardMarkup.keyboard = rowList

        responseMessage.replyMarkup = inlineKeyboardMarkup

        return responseMessage
    }
}