package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.data.entity.ServiceSchedule
import ru.sber.kotlinschool.data.model.UserFlow
import ru.sber.kotlinschool.data.repository.PersonRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.jvm.optionals.getOrNull

@Service
class ProcessService(
    @Autowired val personRepository: PersonRepository,
    @Autowired val serviceProvidedService: ServiceProvidedService,
    @Autowired val scheduleService: ScheduleServiceImpl
) {
    private val userHistory: ConcurrentHashMap<Long, UserFlow> = ConcurrentHashMap()
    fun startStage(stepName: String, user: User): Pair<List<List<String>>, String> {
        userHistory.remove(user.id)
        val userFlow = UserFlow()
        userFlow.steps.set(stepName, "")
        userHistory.set(user.id, userFlow)
        var currentUser = personRepository.findById(user.id).getOrNull()
        if (currentUser == null) {
            currentUser = personRepository.save(
                Person(
                    id = user.id,
                    fio = user.firstName + " " + user.lastName,
                    phone = "",
                    role = PersonRole.CLIENT,
                    firstName = user.firstName
                )
            )
        }
        var buttons: List<List<String>>
        val responseText = "Добро пожаловать, ${user.firstName}!"
        val role = currentUser.role
        when (role) {
            PersonRole.CLIENT -> {
                buttons = getFirstStageClientButtons()
            }
            PersonRole.ADMIN -> {
                buttons = getFirstStageAdminButtons()
            }
        }
        return Pair(buttons, responseText)
    }

    fun getServiceListButtons(stepName: String, userId: Long): List<List<String>> {
        val userFlow = userHistory.get(userId)
        userFlow!!.steps.set(stepName, "")
        userHistory.set(userId, userFlow)
        val serviceProvideds = serviceProvidedService.getServiceProvidedList()
        val elements = ArrayList<String>()
        val buttons: MutableList<List<String>> = mutableListOf(elements)
        serviceProvideds.forEach {
            buttons.add(arrayListOf<String>(it.name))
        }
        buttons.add(arrayListOf("Назад"))
        return buttons
    }

    fun getServiceListMessage(): String {
        val serviceProvideds = serviceProvidedService.getServiceProvidedList()
        val stringBuilder = StringBuilder()
        serviceProvideds.forEach {
            stringBuilder.append(it.name + ", " + it.price + " руб\n")
        }
        return stringBuilder.toString()
    }

    fun getPreviousListButtons(user: User): Pair<List<List<String>>, String> {
        val userFlow = userHistory[user.id]
        val (keyLast, valueLast) = userFlow!!.steps.entries.stream().skip((userFlow.steps.size - 1).toLong())
            .findFirst().get()
        userFlow.steps.remove(keyLast)
        val (keyPrev, valuePrev) = userFlow.steps.entries.stream().skip((userFlow.steps.size - 1).toLong()).findFirst()
            .get()
        var response: Pair<List<List<String>>, String> = Pair(mutableListOf(ArrayList<String>()), "")
        when (keyPrev) {
            "/start" -> {
                response = startStage("/start", user)
            }
            "Список услуг" -> {
                response = Pair(
                    getServiceListButtons("Список услуг", user.id),
                    "Выберите услугу из списка"
                )
            }
            "Выбрана услуга" -> {
                response = Pair(
                    getFreeDaysAsButtons("Выбрана услуга", user.id, valuePrev),
                    "Выберите удобную дату"
                )
            }
            "Мое расписание" -> {
                response = Pair(
                    getBusyDaysAsButtons(user.id, "Мое расписание"),
                    "Выберите дату"
                )
            }
        }

        return response
    }

    fun getFreeDaysAsButtons(stepName: String, userId: Long, serviceProvided: String): List<List<String>> {
        val userFlow = userHistory.get(userId)
        userFlow!!.steps.set(stepName, serviceProvided)
        userHistory.set(userId, userFlow)
        val freeDaysHours = scheduleService.getFreeDays(serviceProvided)
        val buttons: MutableList<List<String>> = mutableListOf(ArrayList<String>())
        freeDaysHours.forEach {
            buttons.add(arrayListOf<String>(it.key))
        }
        buttons.add(arrayListOf("Назад"))

        return buttons
    }

    fun getBusyDaysAsButtons(userId: Long, stepName: String): List<List<String>> {
        val userFlow = userHistory[userId]
        userFlow!!.steps.set(stepName, "")
        userHistory.set(userId, userFlow)
        val busyDaysHours = scheduleService.getBusyDays(userId)
        val buttons: MutableList<List<String>> = mutableListOf(ArrayList<String>())
        busyDaysHours.forEach {
            var hours: String = ""
            it.value.sorted().forEach { hours += it.toString() + ":00 " }
            buttons.add(arrayListOf<String>("${it.key}\n$hours"))
        }
        buttons.add(arrayListOf("Назад"))

        return buttons
    }


    fun saveScheduleRecordWithDate(stepName: String, userId: Long, selectedDate: String): List<List<String>> {
        val buttons: MutableList<List<String>> = mutableListOf(ArrayList<String>())
        val userFlow = userHistory[userId]
        val userRole = personRepository.findById(userId).get().role
        if (userRole == PersonRole.CLIENT) {
            val (keyLast, valueLast) = userFlow!!.steps.entries.stream().skip((userFlow.steps.size - 1).toLong())
                .findFirst().get()
            val serviceProvided = userFlow.steps[keyLast] ?: ""

            val freeDaysHours = scheduleService.getFreeDays(serviceProvided)
            freeDaysHours[selectedDate]?.forEach {
                buttons.add(arrayListOf<String>("$it:00"))
            }
        } else {
            val allRecords =
                scheduleService.getMySchedule().filter {
                    it.executor.id == userId
                            && !it.isVisited
                            && it.date.isAfter(LocalDate.now().minusDays(1))
                }
                    .toCollection(ArrayList())
            allRecords.forEach {
                buttons.add(
                    arrayListOf(
                        "${it.time.hour}:00, ${it.customer.fio ?: it.customer.firstName}, ${it.service.name}, ${it.service.price}"
                    )
                )
            }
        }

        buttons.add(arrayListOf("Назад"))

        userFlow!!.steps.set(stepName, selectedDate)
        userHistory.set(userId, userFlow)

        return buttons
    }

    fun executeRecord(user: User, hour: String): Pair<List<List<String>>, String> {
        val resultMessage = doRecord(user, hour)
        val buttons = startStage("/start", user).first

        return Pair(buttons, resultMessage)
    }

    fun doRecord(user: User, hour: String): String {
        val userFlow = userHistory.get(user.id)
        val (keyLast, valueLast) = userFlow!!.steps.entries.stream().skip((userFlow.steps.size - 1).toLong())
            .findFirst().get()
        val dateRecord = valueLast
        userFlow.steps.remove(keyLast)
        val (keyPrev, valuePrev) = userFlow.steps.entries.stream().skip((userFlow.steps.size - 1).toLong()).findFirst()
            .get()
        val serviceProvadedRecord = valuePrev
        val message = "Вы успешно записались на услугу \"$serviceProvadedRecord\" на дату $dateRecord в $hour часов"
        val FORMATTER = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            .toFormatter(Locale("Ru"))
        val localDateRecord = LocalDate.parse(dateRecord.split(",")[0] + " 2023", FORMATTER)
        val localTimeRecord = LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm"))

        val record = ServiceSchedule(
            id = null,
            insDate = LocalDateTime.now(),
            customer = personRepository.findById(user.id).get(),
            service = serviceProvidedService.getByName(serviceProvadedRecord),
            date = localDateRecord,
            time = localTimeRecord,
            isVisited = false,
            executor = personRepository.findById(920478663).get()
        )
        scheduleService.saveRecord(record)

        return message
    }

    fun getRevenue(stepName: String, user: User): Pair<List<List<String>>, String> {
        val revenue = scheduleService.getRevenueByFinishedSchedule(user.id)
        val resultMessage = "Вы заработали со дня основания : $revenue руб"
        val buttons = startStage("/start", user).first

        return Pair(buttons, resultMessage)
    }

    fun getActionsForScheduleRecord(
        stepName: String,
        user: User,
        recordAsString: String
    ): Pair<List<List<String>>, String> {
        val userFlow = userHistory.get(user.id)
        userFlow!!.steps.set(stepName, recordAsString)
        userHistory.set(user.id, userFlow)
        var resultMessage = "В разработке"
        val buttons = startStage("/start", user).first
        return Pair(buttons, resultMessage)
    }

    companion object {
        fun getFirstStageClientButtons() = listOf(
            listOf("О боте", "О мастере"),
            listOf("Список услуг", "Записаться")
        )

        fun getFirstStageAdminButtons() = listOf(
            listOf("Мое расписание", "Список клиентов"),
            listOf("Рассчитать выручку")
        )
    }
}

