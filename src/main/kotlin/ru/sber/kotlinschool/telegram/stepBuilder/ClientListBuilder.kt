package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("CLIENT_LIST")
class ClientListBuilder : StepBuilder() {

    @Autowired
    private lateinit var userState: UserState

    var clientList = listOf(
        Person(1, "Люк Скайуокер", "+79965512364", "@lukeSkywalker", PersonRole.CLIENT),
        Person(2, "Хан Соло", "+79965512364", "@hanSolo", PersonRole.CLIENT),
        Person(3, "Лея Органа ", "+79965512364", "@leiaOrganaSolo", PersonRole.CLIENT),
        Person(4, "Оби-Ван Кеноби", "+79965512364", "@obiWanKenobi", PersonRole.CLIENT),
        Person(5, "Квай-Гон Джинн", "+79965512364", "@quiGonJinn", PersonRole.CLIENT)
    )

    override fun build(currentStep: Step, chatId: String): SendMessage
    {
        var message = if (currentStep.configParams.isNotEmpty()) {
            val param = currentStep.configParams[0];
            val fio = userState.getTmpParam(chatId, UserParam.valueOf(param))
            prepareClientList(fio);
        } else prepareClientList(null)

        val responseMessage = SendMessage(chatId, message)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title) }

        responseMessage.replyMarkup = getReplyMarkup(list)
        responseMessage.parseMode = ParseMode.HTML

        return responseMessage
    }

    private fun prepareClientList(fio: String?): String
    {
        var clients = clientList

        if (fio != null) {
            clients = clientList.filter { it.fio.lowercase().contains(fio.lowercase()) }
        }

        val clientList = clients.joinToString("\n") { String.format("%15s | %10s | %10s", it.fio, it.phone, it.tLogin) }

        return "Список клиентов: \n " +
                "\n" +
                clientList;
    }
}