package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.stepAction.ActionExecutor
import ru.sber.kotlinschool.telegram.stepBuilder.StepBuilder

@Service
class KotlingradBot : TelegramLongPollingBot() {

    @Value("\${bot.name}")
    private val botName: String = ""

    @Value("\${bot.token}")
    private val token: String = ""

    @Autowired
    private lateinit var scriptService: ScriptService

    @Autowired
    private val stepBuilderMap: Map<String, StepBuilder> = HashMap()

    @Autowired
    private val actionExecutorMap: Map<String, ActionExecutor> = HashMap()

    @Autowired
    private lateinit var userState: UserState

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        if (update != null) {
            if (update.hasMessage()) {
                executeResponseForMessage(update)
            } else if (update.hasCallbackQuery()) {
                executeResponseForReply(update)
            }
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

            if(prevStep!=null && prevStep.executeAction?.isNotBlank() == true)
            {
                currentStep = actionExecutorMap[prevStep.executeAction]?.execute(prevStep, messageText, chatId.toString());
            }

            if(currentStep == null) {
                currentStep = if (prevStep != null)
                    prevStep.children.singleOrNull() { it.title == messageText };
                else scriptService.getFirstStep()
            }

            if(currentStep == null)
                currentStep = prevStep;

            responseMessage =
                stepBuilderMap[currentStep!!.stepType.name]?.build(currentStep, chatId.toString())!!;

            userState.setCurrentStep(chatId.toString(), State(currentStep.id, responseMessage.text))
        }
        execute(responseMessage)
    }

    fun executeResponseForReply(update: Update) {
        val callback = update.callbackQuery
        val message = callback.message
        val chatId = message.chatId
        var responseMessage = SendMessage(chatId.toString(), "Я понимаю только текст") //TODO обработка ошибки
        if (message.hasText()) {
            val prevStepId = userState.getPrevStep(chatId.toString())

            val prevStep: Step? = prevStepId?.let { scriptService.getCurrentStepById(it.stepId) }

            var currentStep: Step? = null

            if(prevStep!=null && prevStep.executeAction?.isNotBlank() == true)
            {
                currentStep = actionExecutorMap[prevStep.executeAction]?.execute(prevStep, callback.data, chatId.toString());
            }

            if(currentStep==null) {
                currentStep = if (prevStep != null)
                prevStep.children[0]
                else scriptService.getFirstStep()
            }

            responseMessage =
                stepBuilderMap[currentStep!!.stepType.name]?.build(currentStep, chatId.toString())!!;

            userState.setCurrentStep(chatId.toString(), State(currentStep.id, responseMessage.text))
        }
        execute(responseMessage)
    }
}