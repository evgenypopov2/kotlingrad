package ru.sber.kotlinschool.telegram.stepActions

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.telegram.entity.Step

@Component("USER_LINK")
class UserLinkAction : Action() {

    override fun execute(currentStep: Step, chatId: String): SendMessage {
        val userMessage =  getUserMessage(Person(1L, "Лея Органа", "545345", "@leyaOrgano", PersonRole.CLIENT),
            currentStep.configParams)
        val responseMessage = SendMessage(chatId, userMessage)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    fun getUserMessage(person: Person, msgType:List<String>): String //TODO
    {
        val message = msgType.map { getMessageByType(person,it) }.joinToString("\n")
        return "Отправить клиенту: \n" +
                "${person.fio} с номером телефона ${person.phone} \n" +
                "\n" +
                "Сообщение:\n" +
                "${message}?\n" +
                "\n";
    }

    fun getMessageByType(person: Person,type: String): String{
       return when (type) {
            "MOVE" -> moveMessage(person)
            "DECLINE" -> declineMessage(person)
            else -> defaultMessage(person)
        }
    }

    fun moveMessage (person: Person): String {
        val revervationDate = "13.02.23" //TODO брать из базы
        val availableDate = "13.02.23" //TODO брать из базы
        val availableTime = "15:30" //TODO брать из базы
        return "Привет, ${person.fio}! Не cмогу тебя принять ${revervationDate}.\n " +
                "Перенесем запись на ${availableDate} в ${availableTime}? "
    }

    fun declineMessage(person: Person): String {
        val revervationDate = "13.02.23" //TODO брать из базы
        return "Привет, ${person.fio}! Не cмогу тебя принять ${revervationDate}.\n " +
                "Свяжусь с тобой через пару дней для переноса записи:)"
    }

    fun defaultMessage(person: Person): String {
        return "Привет, ${person.fio}! Что-то пошло не так"
    }
}