package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlinschool.telegram.stepActions.Action

@Service
class KotlingradBot : TelegramLongPollingBot() {

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""

    @Autowired
    private lateinit var scriptService: ScriptService

    @Autowired
    private val actionMap: Map<String, Action> = HashMap()

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        if (update != null && update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            var responseMessage = SendMessage(chatId.toString(), "Я понимаю только текст") //TODO обработка ошибки
            if (message.hasText()) {
                val messageText = message.text
                //val from = message.from

                var currentStep = scriptService.getCurrentStep(messageText);
                if(currentStep == null) currentStep = scriptService.getFirstStep()
                responseMessage =
                    actionMap[currentStep.actionOnStep.name]?.execute(currentStep, chatId.toString())!!;
            }
            execute(responseMessage)
        }
    }
}