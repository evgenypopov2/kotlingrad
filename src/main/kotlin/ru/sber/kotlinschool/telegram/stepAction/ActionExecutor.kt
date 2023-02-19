package ru.sber.kotlinschool.telegram.stepAction

import ru.sber.kotlinschool.telegram.entity.Step

abstract class ActionExecutor {

    abstract fun execute(prevStep: Step, message: String, chatId: String):Step?

}