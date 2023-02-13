package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.telegram.entity.Step

@Component("USER_INFO")
class UserInfoAction : Action() {

    override fun execute(currentStep: Step, chatId: String): SendMessage {
        val userInfo = getUserInfo(Person(1L, "Лея Органа", "545345", "@leyaOrgano", PersonRole.CLIENT))
        val responseMessage = SendMessage(chatId, userInfo)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title) }

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    fun getUserInfo(person: Person): String //TODO
    {
        return "Информация по клиенту: \n" +
                "\n" +
                "Имя клиента: ${person.fio} \n" +
                "Номер телефона: ${person.phone} \n" +
                "Логин: ${person.tLogin} \n"
    }

}