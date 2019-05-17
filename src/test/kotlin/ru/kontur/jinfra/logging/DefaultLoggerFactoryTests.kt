package ru.kontur.jinfra.logging

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.context.ContextLogger
import ru.kontur.jinfra.logging.context.LoggingContext

class DefaultLoggerFactoryTests {

    @Test
    fun current_class_logger() {
        val logger = Logger.currentClass()
        logger.info { "Log message" }
    }

    @Test
    fun current_class_context_logger() {
        val logger = ContextLogger.currentClass()
        runBlocking {
            withContext(LoggingContext.with("foo", "123")) {
                logger.info { "Log message" }
            }
        }
    }

}
