package ru.kontur.jinfra.logging

import org.junit.jupiter.api.Test

private val topLevelLogger = Logger.currentClass().withoutContext()

class DefaultLoggerFactoryTests {

    private val classLogger = Logger.currentClass().withoutContext()

    @Test
    fun current_class_logger() {
        classLogger.info { "Class logger message" }
    }

    @Test
    fun companion_current_class_logger() {
        companionLogger.info { "Companion logger message" }
    }

    @Test
    fun top_level_logger() {
        topLevelLogger.info { "Top level logger message" }
    }

    companion object {

        private val companionLogger = Logger.currentClass().withoutContext()
            .addContext("test", "testValue")

    }

}
