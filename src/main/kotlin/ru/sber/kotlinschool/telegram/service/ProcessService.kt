package ru.sber.kotlinschool.telegram.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.User
import ru.sber.kotlinschool.data.entity.Person
import ru.sber.kotlinschool.data.entity.PersonRole
import ru.sber.kotlinschool.data.entity.ServiceRegistration
import ru.sber.kotlinschool.data.model.UserFlow
import ru.sber.kotlinschool.data.repository.PersonRepository
import ru.sber.kotlinschool.data.service.ServiceProvidedService
import ru.sber.kotlinschool.data.service.ServiceRegistrationService
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ProcessService(
    @Autowired val personRepository: PersonRepository,
    @Autowired val serviceProvidedService: ServiceProvidedService,
    @Autowired val serviceRegistrationService: ServiceRegistrationService
) {
    private val userHistory: ConcurrentHashMap<Long, UserFlow> = ConcurrentHashMap()
    fun startStage(stepName: String, user: User): Pair<List<List<String>>, String> {
        userHistory.remove(user.id)
        val userFlow = UserFlow()
        userFlow.steps[stepName] = ""
        userHistory[user.id] = userFlow
        var currentUser = personRepository.findById(user.id).orElse(null)

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
        val responseText = "Добро пожаловать, ${user.firstName}!"

        val buttons = when (currentUser.role) {
            PersonRole.CLIENT -> getFirstStageClientButtons()
            PersonRole.ADMIN -> getFirstStageAdminButtons()
            else -> emptyList()
        }
        return Pair(buttons, responseText)
    }

    fun getServiceListButtons(stepName: String, userId: Long): List<List<String>> {
        val userFlow = userHistory[userId]
        userFlow!!.steps[stepName] = ""
        userHistory[userId] = userFlow
        val servicesProvided = serviceProvidedService.getServiceProvidedList()
        val elements = ArrayList<String>()
        val buttons: MutableList<List<String>> = mutableListOf(elements)
        servicesProvided.forEach {
            buttons.add(arrayListOf(it.name))
        }
        buttons.add(arrayListOf("Назад"))
        return buttons
    }

    fun getServiceListMessage(): String {
        val stringBuilder = StringBuilder()
        serviceProvidedService.getServiceProvidedList().forEach {
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
        val userFlow = userHistory[userId]
        userFlow!!.steps[stepName] = serviceProvided
        userHistory[userId] = userFlow
        val freeDaysHours = serviceRegistrationService.getFreeDays(serviceProvided)
        val buttons: MutableList<List<String>> = mutableListOf(ArrayList<String>())
        freeDaysHours.forEach {
            buttons.add(arrayListOf(it.key))
        }
        buttons.add(arrayListOf("Назад"))

        return buttons
    }

    fun getBusyDaysAsButtons(userId: Long, stepName: String): List<List<String>> {
        val userFlow = userHistory[userId]
        userFlow!!.steps[stepName] = ""
        userHistory[userId] = userFlow
        val busyDaysHours = serviceRegistrationService.getBusyDays(userId)
        val buttons: MutableList<List<String>> = mutableListOf(ArrayList<String>())
        busyDaysHours.forEach {
            var hours: String = ""
            it.value.sorted().forEach { hours += it.toString() + ":00 " }
            buttons.add(arrayListOf("${it.key}\n$hours"))
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

            val freeDaysHours = serviceRegistrationService.getFreeDays(serviceProvided)
            freeDaysHours[selectedDate]?.forEach {
                buttons.add(arrayListOf("$it:00"))
            }
        } else {
            val allRecords =
                serviceRegistrationService.getMySchedule().filter {
                    it.executor.id == userId
                            && !it.isVisited
                            && it.date.isAfter(LocalDate.now().minusDays(1))
                }
                    .toCollection(ArrayList())
            allRecords.forEach {
                buttons.add(
                    arrayListOf(
                        "${it.time.hour}:00, ${it.client.fio ?: it.client.firstName}, ${it.service.name}, ${it.service.price}"
                    )
                )
            }
        }

        buttons.add(arrayListOf("Назад"))

        userFlow!!.steps[stepName] = selectedDate
        userHistory[userId] = userFlow

        return buttons
    }

    fun executeRecord(user: User, hour: String): Pair<List<List<String>>, String> {
        val resultMessage = doRecord(user, hour)
        val buttons = startStage("/start", user).first

        return Pair(buttons, resultMessage)
    }

    fun doRecord(user: User, hour: String): String {
        val userFlow = userHistory[user.id]
        val (keyLast, valueLast) = userFlow!!.steps.entries.stream().skip((userFlow.steps.size - 1).toLong())
            .findFirst().get()
        val dateRecord = valueLast
        userFlow.steps.remove(keyLast)
        val (keyPrev, valuePrev) = userFlow.steps.entries.stream().skip((userFlow.steps.size - 1).toLong()).findFirst()
            .get()
        val serviceProvidedRecord = valuePrev
        val message = "Вы успешно записались на услугу \"$serviceProvidedRecord\" на дату $dateRecord в $hour часов"
        val FORMATTER = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            .toFormatter(Locale("Ru"))
        val localDateRecord = LocalDate.parse(dateRecord.split(",")[0] + " 2023", FORMATTER)
        val localTimeRecord = LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm"))

        val record = ServiceRegistration(
            client = personRepository.findById(user.id).get(),
            service = serviceProvidedService.getByName(serviceProvidedRecord),
            date = localDateRecord,
            time = localTimeRecord,
            isVisited = false,
            executor = personRepository.findById(920478663).get()
        )
        serviceRegistrationService.save(record)

        return message
    }

    fun getRevenue(stepName: String, user: User): Pair<List<List<String>>, String> {
        val revenue = serviceRegistrationService.getRevenueByFinishedSchedule(user.id)
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
        userFlow!!.steps[stepName] = recordAsString
        userHistory[user.id] = userFlow
        val resultMessage = "В разработке"
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

