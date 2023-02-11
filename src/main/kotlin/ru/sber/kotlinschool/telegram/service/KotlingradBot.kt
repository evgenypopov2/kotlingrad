package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlinschool.telegram.const.Icon

@Service
class KotlingradBot: TelegramLongPollingBot()
{

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""


    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
            if (update!=null && update.hasMessage()) {
                val message = update.message
                val chatId = message.chatId
                val responseText = if (message.hasText()) {
                    val messageText = message.text
                    val from = message.from
                    when {
                        messageText == "/start" -> "Добро пожаловать, ${from.firstName}!"
                        messageText.startsWith("Кнопка ") -> "Вы нажали кнопку" // обработка нажатия кнопки
                        else -> "Вы написали: *$messageText*"
                    }
                } else {
                    "Я понимаю только текст"
                }
                sendNotification(chatId, responseText)
            }
    }

    private fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        // добавляем 4 кнопки
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("${Icon.TIME.value()} Мое Расписание", "${Icon.CLIENTS.value()} Список клиентов"),
                listOf("${Icon.MONEY.value()} Рассчитать выручку")
            )
        )
        execute(responseMessage)
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }
}