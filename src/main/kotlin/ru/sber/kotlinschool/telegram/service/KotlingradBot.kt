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
import ru.sber.kotlinschool.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*


@Service
class KotlingradBot(
    @Autowired val scheduleService: ScheduleServiceImpl,
    @Autowired val processService: ProcessService,
    @Autowired val serviceProvidedService: ServiceProvidedService

) : TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(KotlingradBot::class.java)

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""

    @Value("\${bot.about.for-client}")
    private val aboutBotForClient: String = ""

    @Value("\${bot.about.master}")
    private val aboutMaster: String = ""

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {

        if (update != null && update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            var buttons: List<List<String>> = listOf(ArrayList<String>())
            // TODO попросить указать телефон

            val responseText = if (message.hasText()) {
                val utils = Utils()
                val messageText = message.text
                val currentUser = message.from
                logger.info("Request $messageText from userId ${currentUser.id} ${currentUser.firstName}")
                logger.info("Full info about user: $currentUser")
                val serviceProvidedName = messageText.split(",")
                if (serviceProvidedName.size == 1) {
                    when {
                        messageText == "/start" -> {
                            val result = processService.startStage("/start", currentUser)
                            buttons = result.first
                            result.second
                        }

                        messageText == "О боте" -> {
                            buttons = ProcessService.getFirstStageClientButtons()
                            aboutBotForClient
                        }

                        messageText == "О мастере" -> {
                            buttons = ProcessService.getFirstStageClientButtons()
                            aboutMaster
                        }

                        messageText == "Список услуг" -> {
                            buttons = processService.startStage("/start", currentUser).first
                            processService.getServiceListMessage()
                        }

                        messageText == "Записаться" -> {
                            buttons = processService.getServiceListButtons("Список услуг", currentUser.id)
                            "Выберите услугу из списка"
                        }

                        messageText == "Назад" -> {
                            val response = processService.getPreviousListButtons(currentUser)
                            buttons = response.first
                            response.second
                        }

                        messageText in serviceProvidedService.getServiceProvidedNames() -> {
                            buttons = processService.getFreeDaysAsButtons("Выбрана услуга", currentUser.id, messageText)

                            "Выберите удобную дату"
                        }

                        messageText.contains(Regex("""\d{2}:\d{2}""")) -> {
                            val result = processService.executeRecord(currentUser, messageText)
                            buttons = result.first
                            result.second
                        }

                        messageText.contains(Regex("""\d{2}\s\D{3,8}\s\d{4},\s\D{2}""")) -> {

                            val formatter = DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .append(DateTimeFormatter.ofPattern("dd MMMM yyyy, EE"))
                                .toFormatter(Locale("Ru"))

                            val parsed = formatter.parse(messageText)

                            buttons = utils.getButtonsList(
                                scheduleService.getUserShedule(
                                    currentUser.id,
                                    LocalDate.of(
                                        parsed.get(ChronoField.YEAR),
                                        parsed.get(ChronoField.MONTH_OF_YEAR),
                                        parsed.get(ChronoField.DAY_OF_MONTH)
                                    )
                                )
                            )
                            "Выбрана дата $messageText"
                        }

                        messageText.startsWith("Мое расписание") -> {
                            buttons = processService.getBusyDaysAsButtons(currentUser.id, messageText)
                            "Выберите дату"
                        }

                        messageText.startsWith("Список клиентов") -> {
                            val result = processService.startStage("/start", currentUser)
                            "В разработке"
                        }

                        messageText.startsWith("Рассчитать выручку") -> {
                            val result = processService.getRevenue("revenue", currentUser)
                            buttons = result.first
                            result.second
                        }
                        else -> "Вы написали: $messageText"
                    }
                } else if (serviceProvidedName.size == 2) {
                    //секция для дней
                    buttons = processService.saveScheduleRecordWithDate("Выбрана дата", currentUser.id, messageText)
                    "Выберите время"
                } else {
                    //секция для вывода меню управления выбранной записью в расписании админа
                    val result = processService.getActionsForScheduleRecord("Выбрана запись", currentUser, messageText)
                    buttons = result.first
                    result.second
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