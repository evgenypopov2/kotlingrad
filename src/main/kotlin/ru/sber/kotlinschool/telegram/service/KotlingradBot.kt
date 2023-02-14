package ru.sber.kotlinschool.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlinschool.telegram.const.Icon
import ru.sber.kotlinschool.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*


@Service
class KotlingradBot(@Autowired val scheduleService: ScheduleServiceImpl) : TelegramLongPollingBot() {

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""


    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {

        if (update != null && update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            var buttons : List<List<String>> = listOf(ArrayList<String>())
            val utils = Utils()

            val responseText = if (message.hasText()) {
                val messageText = message.text
                val from = message.from
                when {
                    messageText == "/start" -> {
                        buttons = listOf(
                            listOf("${Icon.TIME.value()} Мое Расписание", "${Icon.CLIENTS.value()} Список клиентов"),
                            listOf("${Icon.MONEY.value()} Рассчитать выручку")
                        )
                        "Добро пожаловать, ${from.firstName}!"
                    }

                    messageText.contains(Regex("""\d{2}\s\D{3,8}\s\d{4},\s\D{2}""")) -> {

                        val formatter = DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .append(DateTimeFormatter.ofPattern("dd MMMM yyyy, EE"))
                            .toFormatter(Locale("Ru"))

                        val parsed = formatter.parse(messageText)

                        buttons = utils.getButtonsList(
                            scheduleService.getUserShedule(
                                from.id,
                                LocalDate.of(
                                    parsed.get(ChronoField.YEAR),
                                    parsed.get(ChronoField.MONTH_OF_YEAR),
                                    parsed.get(ChronoField.DAY_OF_MONTH)
                                )
                            )
                        )
                        "Выбрана дата $messageText"
                    }

                    messageText.startsWith("${Icon.TIME.value()} Мое Расписание") -> {
                        buttons = utils.getButtonsList(Utils().getWeeksDates())
                        "Выберите дату на текущей неделе..." // обработка нажатия кнопки
                    }

                    else -> "Вы написали: *$messageText*"
                }
            } else {
                "Я понимаю только текст"
            }
            sendNotification(chatId, responseText, buttons)
        }
    }

    private fun sendNotification(chatId: Long, responseText: String, buttons: List<List<String>>) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        if (buttons.size > 0) {
            responseMessage.enableMarkdown(true)
            responseMessage.replyMarkup = getReplyMarkup(buttons)
            execute(responseMessage)
        }
    }

/*
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
*/
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