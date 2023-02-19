package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.sber.kotlinschool.data.service.ServicePerson
import ru.sber.kotlinschool.data.service.ServiceSchedule
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState
import ru.sber.kotlinschool.telegram.stepBuilder.StepBuilder
import java.time.LocalTime



@Component("ADMIN_DAY_SCHEDULE")
class DayScheduleStepBuilder(@Autowired val scheduleService: ServiceSchedule,
                             @Autowired val personService: ServicePerson,
                             @Autowired val userState: UserState
) : StepBuilder() {

    //private val logger = LoggerFactory.getLogger(DayScheduleStepBuilder::class.java)

    @Value("\${admin.startWorkinHour}")
    private val  start: String = ""

    @Value("\${admin.endWorkingHour}")
    private val  end: String = ""

    override fun build(currentStep: Step, chatId: String): SendMessage {

        val selectedDate = scheduleService.getSelectedDate(userState.getTmpParam(chatId, UserParam.CALLBACK_DATA))

        val list = scheduleService.getDaySchedule(
            selectedDate,
            personService.findPersonById(chatId.toLong()).get(),
            LocalTime.of(start.toInt(), 0, 0, 0),
            LocalTime.of(end.toInt(), 0, 0, 0)
        )

        val responseMessage = SendMessage(chatId, if (list.isNotEmpty()) currentStep.messageForUser else "На указанную дату записей нет")
        responseMessage.enableMarkdown(false)

        val inlineKeyboardMarkup = InlineKeyboardMarkup() //Создаем объект разметки клавиатуры
        val keyboardButtonsRow: MutableList<InlineKeyboardButton> = ArrayList()
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList() //Создаём ряд

        list.forEach {
            val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
            val inlineKeyboardButton1 = InlineKeyboardButton()
            inlineKeyboardButton1.text = it.value
            inlineKeyboardButton1.callbackData = it.key.toString()
            keyboardButtonsRow1.add(inlineKeyboardButton1)
            rowList.add(keyboardButtonsRow1)
        }

        rowList.add(keyboardButtonsRow)
        inlineKeyboardMarkup.keyboard = rowList
        responseMessage.replyMarkup = inlineKeyboardMarkup

        return responseMessage
    }


}