package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlinschool.data.service.PersonService
import ru.sber.kotlinschool.data.service.ServiceRegistrationService
import ru.sber.kotlinschool.telegram.entity.Step
import java.time.LocalDate


@Component("CREATE_CALENDAR")
class CalendarStepBuilder(
    @Autowired val registrationService: ServiceRegistrationService,
    @Autowired val personService: PersonService
) : StepBuilder() {

    override fun build(currentStep: Step, chatId: String): SendMessage {
        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(false)

        val inlineKeyboardMarkup = InlineKeyboardMarkup() //Создаем объект разметки клавиатуры
        val keyboardButtonsRow: MutableList<InlineKeyboardButton> = ArrayList()
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList() //Создаём ряд

        val captions = registrationService.getWeekSchedule(LocalDate.now(), personService.findById(chatId.toLong()).get())

        captions.forEach {
            val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
            val inlineKeyboardButton1 = InlineKeyboardButton()
            inlineKeyboardButton1.text = it
            inlineKeyboardButton1.callbackData = it
            keyboardButtonsRow1.add(inlineKeyboardButton1)
            rowList.add(keyboardButtonsRow1)
        }

        rowList.add(keyboardButtonsRow)
        inlineKeyboardMarkup.keyboard = rowList
        responseMessage.replyMarkup = inlineKeyboardMarkup

        return responseMessage
    }

}