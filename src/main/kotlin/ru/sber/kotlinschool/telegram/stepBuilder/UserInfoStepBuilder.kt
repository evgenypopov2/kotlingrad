package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.service.ServiceRegistrationService
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("USER_INFO")
class UserInfoStepBuilder(@Autowired val registrationService: ServiceRegistrationService,
                          @Autowired val userState: UserState
) : StepBuilder() {

    override fun build(currentStep: Step, chatId: String): SendMessage {

        val id: Long = userState.getTmpParam(chatId, UserParam.CALLBACK_DATA)?.toLong() ?: 0

        val userInfo = registrationService.getScheduleDetailInfo(id)
        val responseMessage = SendMessage(chatId, userInfo)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title) }

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

}