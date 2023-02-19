package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState

@Component("CLIENT_LIST")
class ClientListBuilder: StepBuilder() {

    @Autowired
    private lateinit var userState: UserState

    override fun build(currentStep: Step, chatId: String): SendMessage
    {
        if(currentStep.configParams.isNotEmpty())
        {
            val param = currentStep.configParams[0];
            val fio = userState.getTmpParam(chatId, UserParam.valueOf(param))
            //TODO поиск по полученному параметру
        }
        val message  = prepareClientList();
        val responseMessage = SendMessage(chatId, message)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }

    private fun prepareClientList(): String
    {
        return "Список клиентов: \n" +
                "1. Люк Скайуокер   |   +79965512364  | @lukeSkywalker \n" +
                "2. Хан Соло        |   +79965512364  | @hanSolo \n" +
                "3. Лея Органа      |   +79965512364  | @leiaOrganaSolo \n" +
                "4. Оби-Ван Кеноби  |   +79965512364  | @obiWanKenobi \n" +
                "5. Квай-Гон Джинн  |   +79965512364  | @quiGonJinn";
    }
}