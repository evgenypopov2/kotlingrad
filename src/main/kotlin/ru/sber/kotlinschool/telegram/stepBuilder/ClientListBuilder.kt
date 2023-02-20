package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.data.service.PersonService
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("CLIENT_LIST")
class ClientListBuilder(
    private val personService: PersonService
) : StepBuilder() {

    @Autowired
    private lateinit var userState: UserState

    override fun build(currentStep: Step, chatId: String): SendMessage
    {
        val message = if (currentStep.configParams.isNotEmpty()) {
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
        var clients = personService.findAllClients()

        if (fio != null) {
            clients = clients.filter { it.fio.lowercase().contains(fio.lowercase()) }
        }

        val clientList = clients.joinToString("\n") { String.format("%15s | %10s", it.fio, it.phone) }

        return "Список клиентов: \n " +
                "\n" +
                clientList;
    }
}