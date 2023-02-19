package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("PROFIT_CALC")
class ProfitCalculation : StepBuilder() {

    @Autowired
    private lateinit var userState: UserState

    override fun build(currentStep: Step, chatId: String): SendMessage {

        val period = if (currentStep.configParams.isNotEmpty()) {
            getPeriod(currentStep.configParams[0])
        } else getPeriodFromState(chatId)

        val message = "Сумма за $period составила: " //TODO брать из базы

        val responseMessage = SendMessage(chatId, message)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title) }

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    fun getPeriod(period: String): String {
        return when (period) {
            "MONTH" -> "месяц"
            "WEEK" -> "неделю"
            else -> "Ошибка получения периода расчета"
        }
    }

    fun getPeriodFromState(chatId: String): String {
        var period = userState.getTmpParam(chatId, UserParam.PERIOD)
        if (period == null)
            period = "Ошибка получения периода расчета"
        return period
    }
}