package ru.sber.kotlinschool.telegram.stepBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.sber.kotlinschool.telegram.entity.Step
import ru.sber.kotlinschool.telegram.entity.UserParam
import ru.sber.kotlinschool.telegram.service.UserState
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.ws.rs.core.UriBuilder

@Component("CONNECT_WITH_USER")
class UserConnectStepBuilder : StepBuilder()
{
    @Value("\${bot.token}")
    private val token: String = ""

    @Autowired
    private lateinit var userState: UserState

    override fun build(currentStep: Step, chatId: String): SendMessage
    {
        val CHAT_ID = "151760076" // TODO get from DB Что бы протестировать возьми свой из полученного  Update

        val message = userState.getTmpParam(chatId, UserParam.MESSAGE_FOR_USER)

        val client: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .build()

        val builder: UriBuilder = UriBuilder
            .fromUri("https://api.telegram.org")
            .path("/{token}/sendMessage")
            .queryParam("chat_id", CHAT_ID)
            .queryParam("text", message)

        val request: HttpRequest = HttpRequest.newBuilder()
            .GET()
            .uri(builder.build("bot$token"))
            .timeout(Duration.ofSeconds(5))
            .build()

        val response: HttpResponse<String> = client
            .send(request, HttpResponse.BodyHandlers.ofString())

        val responseMessage = SendMessage(chatId, currentStep.messageForUser)
        responseMessage.enableMarkdown(true)
        val list = currentStep.children.map { nextStep -> listOf(nextStep.title)}

        responseMessage.replyMarkup = getReplyMarkup(list)

        return responseMessage
    }
}