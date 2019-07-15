package ru.kontur.jinfra.logging

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.test.MockBackend
import ru.kontur.jinfra.logging.test.NoDecorFactory

class LoggerTests {

    @Test
    fun message_with_error() = test {
        val error = Throwable()

        logger.log(LogLevel.INFO, error) { "message" }

        assertEquals(1, backend.events.size)
        assertEquals("message", backend.events[0].message)
        assertEquals(error, backend.events[0].error)
    }

    @Test
    fun log_uses_context() = test {
        val expectedContext = LoggingContext.with("userId", 123)

        withContext(expectedContext) {
            logger.info { "message" }
        }

        assertEquals(1, backend.events.size)
        assertEquals(expectedContext, backend.events[0].context)
    }

    @Test
    fun is_enabled_uses_context() = test {
        // Messages with this context will not pass MockBackend.isEnabled() check
        val uninterestingContext = LoggingContext.current().add("ignore", "true")

        withContext(uninterestingContext) {
            logger.info { "message" }
        }

        assertEquals(0, backend.events.size)
    }

    @Test
    fun transform_to_context_logger_and_back_is_same() = test {
        assertSame(logger, logger.withoutContext().withCoroutineContext())
    }

    @Test
    fun without_context_uses_empty_context() = test {
        assertEquals(LoggingContext.EMPTY, logger.withoutContext().context)
    }

    @Test
    fun with_context_empty() = test {
        assertEquals(logger.withoutContext().context, logger.withContext(LoggingContext.EMPTY).context)
    }

    @Test
    fun with_context() = test {
        val context = LoggingContext.with("foo", "bar")

        assertEquals(context, logger.withContext(context).context)
    }

    @Test
    fun caller_info_correct() = test {
        logger.info { "Message" }

        val event = backend.events.single()
        val loggedFrame = event.caller
        val expectedFrame = Throwable().stackTrace[0]

        assertEquals(expectedFrame.fileName, loggedFrame.fileName)
        assertEquals(expectedFrame.className, loggedFrame.className)
        assertEquals(expectedFrame.methodName, loggedFrame.methodName)
    }

    @Test
    fun level_debug() = testLevel(LogLevel.DEBUG) { debug { it } }

    @Test
    fun level_debug_filtered() = testLevelFiltered(LogLevel.DEBUG) { debug { it } }

    @Test
    fun level_info() = testLevel(LogLevel.INFO) { info { it } }

    @Test
    fun level_info_filtered() = testLevelFiltered(LogLevel.INFO) { info { it } }

    @Test
    fun level_warn() = testLevel(LogLevel.WARN) { warn { it } }

    @Test
    fun level_warn_filtered() = testLevelFiltered(LogLevel.WARN) { warn { it } }

    @Test
    fun level_error() = testLevel(LogLevel.ERROR) { error { it } }

    @Test
    fun level_error_filtered() = testLevelFiltered(LogLevel.ERROR) { error { it } }

    private fun testLevel(level: LogLevel, logMethod: suspend Logger.(String) -> Unit) = test {
        backend.level = level

        logger.log(level) { "message" }
        logger.logMethod("message")

        assertEquals(2, backend.events.size)
        assertEquals(level, backend.events[0].level)
        assertEquals(level, backend.events[1].level)
    }

    private fun testLevelFiltered(level: LogLevel, logMethod: suspend Logger.(String) -> Unit) = test {
        backend.level = null

        logger.log(level) { "message" }
        logger.logMethod("message")

        assertEquals(0, backend.events.size)
    }

    private fun test(block: suspend TestContext.() -> Unit) {
        val backend = MockBackend()
        val logger = Logger(backend, NoDecorFactory)
        val context = TestContext(logger, backend)

        runBlocking {
            context.block()
        }
    }

    private class TestContext(
        val logger: Logger,
        val backend: MockBackend
    )

}
