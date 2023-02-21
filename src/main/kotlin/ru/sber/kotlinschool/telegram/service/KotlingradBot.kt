package ru.sber.kotlinschool.telegram.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.data.service.PersonService
import ru.sber.kotlinschool.data.service.ServiceProvidedService
import ru.sber.kotlinschool.data.service.ServiceRegistrationService
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.stepAction.ActionExecutor
import ru.sber.kotlinschool.telegram.stepBuilder.StepBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*


@Service
class KotlingradBot(
    private val personService: PersonService,
    private val processService: ProcessService,
    private val serviceProvidedService: ServiceProvidedService,
    private val serviceRegistrationService: ServiceRegistrationService
) : TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(KotlingradBot::class.java)

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""

    @Value("#{propertiesFileMapping['bot.about.for-client']}")
    private val aboutBotForClient: String = ""

    @Value("#{propertiesFileMapping['bot.about.master']}")
    private val aboutMaster: String = ""

    @Autowired
    private lateinit var scriptService: ScriptService

    @Autowired
    private lateinit var userState: UserState

    @Autowired
    private val stepBuilderMap: Map<String, StepBuilder> = HashMap()

    @Autowired
    private val actionExecutorMap: Map<String, ActionExecutor> = HashMap()

    private val updateHandlers: Map<String, (Update) -> Unit> = mapOf(
        Pair(PersonRole.ADMIN.name) { update: Update -> guzalUpdateHandler(update) },
        Pair(PersonRole.CLIENT.name) { update: Update -> alexeyUpdateHandler(update) },
   )

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        if (update != null) {
            alexeyUpdateHandler(update)
            //updateHandlers[determineUserRole(update).name]?.invoke(update)
        }
    }

    private fun determineUserRole(update: Update): PersonRole =
        if (!update.hasMessage()) {
            PersonRole.ADMIN
        } else {
            val message = update.message
            val userId = message.chatId

            val user = if (userId != null)
                personService.findById(userId).orElseGet { personService.save(newUserFromMessage(message)) }
            else
                defaultUser()

            user.role
        }

    private fun guzalUpdateHandler(update: Update) {
        if (update.hasMessage()) {
            executeResponseForMessage(update)
        } else if (update.hasCallbackQuery()) {
            executeResponseForReply(update)
        }
    }

    fun executeResponseForMessage(update: Update) {

        val message = update.message
        val chatId = message.chatId
        var responseMessage = SendMessage(chatId.toString(), "Я понимаю только текст") //TODO обработка ошибки

        if (message.hasText()) {
            val messageText = message.text
            val prevState = userState.getPrevStep(chatId.toString())
            val prevStep: Step? = prevState?.let { scriptService.getCurrentStepById(it.stepId) }
            var currentStep: Step? = null

            if(prevStep != null && prevStep.executeAction?.isNotBlank() == true) {
                currentStep = actionExecutorMap[prevStep.executeAction]?.execute(prevStep, messageText, chatId.toString())
            }

            if(currentStep == null) {
                currentStep = if (prevStep != null)
                    prevStep.children.singleOrNull() { it.title == messageText }
                else scriptService.getFirstStep()
            }

            if(currentStep == null) {
                currentStep = prevStep
            }

            responseMessage = stepBuilderMap[currentStep!!.stepType.name]?.build(currentStep, chatId.toString())!!
            userState.setCurrentStep(chatId.toString(), State(currentStep.id, responseMessage.text))
        }

        if(responseMessage.replyMarkup is InlineKeyboardMarkup) {
            cleanReplyKeyBoard(chatId.toString())
        }
        execute(responseMessage)
    }

    fun executeResponseForReply(update: Update) {

        val callback = update.callbackQuery
        val message = callback.message
        val chatId = message.chatId
        var responseMessage = SendMessage(chatId.toString(), "Я понимаю только текст") //TODO обработка ошибки

        if (message.hasText()) {

            editMsgReply(callback)

            val prevStepId = userState.getPrevStep(chatId.toString())
            val prevStep: Step? = prevStepId?.let { scriptService.getCurrentStepById(it.stepId) }
            var currentStep: Step? = null

            if(prevStep != null && prevStep.executeAction?.isNotBlank() == true) {
                currentStep = actionExecutorMap[prevStep.executeAction]?.execute(prevStep, callback.data, chatId.toString())
            }

            if(currentStep == null) {
                currentStep = if (prevStep != null)
                    prevStep.children[0]
                else
                    scriptService.getFirstStep()
            }

            userState.updateTmpMap(chatId.toString(), UserParam.CALLBACK_DATA, callback.data)
            responseMessage = stepBuilderMap[currentStep.stepType.name]?.build(currentStep, chatId.toString())!!
            userState.setCurrentStep(chatId.toString(), State(currentStep.id, responseMessage.text))
        }
        execute(responseMessage)
    }

    fun cleanReplyKeyBoard(chatId: String)
    {
        val emptyMsg = SendMessage(chatId, "Выберите из меню сообщения")
        val removeKeyBoard = ReplyKeyboardRemove()
        removeKeyBoard.removeKeyboard = true
        emptyMsg.replyMarkup = removeKeyBoard
        execute(emptyMsg)
    }

    fun editMsgReply(callback: CallbackQuery)
    {
        val editMessageReplyMarkup = EditMessageReplyMarkup()
        editMessageReplyMarkup.chatId = callback.from.id.toString()
        editMessageReplyMarkup.messageId = callback.message.messageId
        execute(editMessageReplyMarkup)

        val editMessageText = EditMessageText()
        editMessageText.chatId = callback.from.id.toString()
        editMessageText.messageId = callback.message.messageId
        editMessageText.text = "Выбрано значение ${callback.data}"
        execute(editMessageText)
        editMessageText.parseMode = ParseMode.HTML
    }

    // client message handler
    private fun alexeyUpdateHandler(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            var buttons: List<List<String>> = listOf(ArrayList<String>())
            // TODO попросить указать телефон

            val responseText = if (message.hasText()) {
                val messageText = message.text
                val currentUser = message.from
                logger.info("Request $messageText from userId ${currentUser.id} ${currentUser.firstName}")
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
                            // TODO проверить доступные дни, у которых есть свободные часы для записи
                            buttons = processService.getFreeDaysAsButtons("Выбрана услуга", currentUser.id, messageText)

                            // buttons = processService.getServiceListButtons("Список услуг", currentUser.id)
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

                            buttons = Utils.getButtonsList(
                                serviceRegistrationService.getUserSchedule(
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
                    // секция для дней
                    buttons = processService.saveScheduleRecordWithDate("Выбрана дата", currentUser.id, messageText)
                    "Выберите время"
                } else {
                    // секция для записей в расписании админа
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
        if (buttons.isNotEmpty()) {
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

    private fun defaultUser() = Person(-1, "Unknown user", "no phone", PersonRole.CLIENT, "no name")
    private fun newUserFromMessage(message: Message) =
        Person(0, message.from.firstName, "", PersonRole.CLIENT, message.from.firstName)
}