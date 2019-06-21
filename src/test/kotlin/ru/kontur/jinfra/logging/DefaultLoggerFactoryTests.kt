package ru.kontur.jinfra.logging

import org.junit.jupiter.api.Test

class DefaultLoggerFactoryTests {

    @Test
    fun current_class_logger() {
        val logger = Logger.currentClass().withoutContext()
        logger.info { "Log message" }
    }

    @Test
    fun companion_current_class_logger() {
        companionLogger.info { "Log message" }
    }

    companion object {

        private val companionLogger = Logger.currentClass().withoutContext()
            .addContext("test", "testValue")

    }

}
