package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.telegram.entity.Step

@Component("USER_INFO")
class UserInfoAction: Action()
{

    override fun execute(currentStep: Step, chatId: String): SendMessage {
        val userInfo =  getUserInfo(Person(1L, "ФИО", "545345", "@53453454", PersonRole.CLIENT))
        val responseMessage = SendMessage(chatId, userInfo)
        responseMessage.enableMarkdown(true)
        val list = currentStep.nextSteps.map { nextStep -> listOf(nextStep)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    fun getUserInfo(person: Person): String //TODO
    {
        return "Информация по клиенту: \n" +
                "\n" +
                "Имя клиента: ${person.fio} \n" +
                "Номер телефона: ${person.phone} \n"+
                "Логин: ${person.tLogin} \n"
    }

}