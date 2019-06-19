package ru.kontur.jinfra.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.test.MockBackend
import ru.kontur.jinfra.logging.test.NoDecorFactory

class ContextLoggerTests {

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
        val logger = logger.addContext("foo", "bar")

        logger.info { "message" }

        assertEquals(logger.context, backend.events[0].context)
    }

    @Test
    fun is_enabled_uses_context() = test {
        // Messages with this context will not pass MockBackend.isEnabled() check
        val uninterestingContext = LoggingContext.EMPTY.add("ignore", "true")

        logger.withContext(uninterestingContext).info { "message" }

        assertEquals(0, backend.events.size)
    }

    @Test
    fun default_context_is_empty() = test {
        assertEquals(LoggingContext.EMPTY, logger.context)
    }

    @Test
    fun with_context() = test {
        val someContext = LoggingContext.EMPTY.add("foo", "bar")

        assertEquals(someContext, logger.withContext(someContext).context)
    }

    @Test
    fun with_context_not_merged() = test {
        val someContext = LoggingContext.EMPTY.add("foo", "bar")
        val newContext = LoggingContext.EMPTY.add("bar", "baz")

        assertEquals(newContext, logger.withContext(someContext).withContext(newContext).context)
    }

    @Test
    fun add_context() = test {
        val expectedContext = LoggingContext.EMPTY
            .add("foo", "bar")
            .add("bar", "baz")

        val actualContext = logger
            .addContext("foo", "bar")
            .addContext("bar", "baz")
            .context

        assertEquals(expectedContext, actualContext)
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
    fun level_trace() = testLevel(LogLevel.TRACE) { trace { it } }

    @Test
    fun level_trace_filtered() = testLevelFiltered(LogLevel.TRACE) { trace { it } }

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

    private fun testLevel(level: LogLevel, logMethod: ContextLogger.(String) -> Unit) = test {
        backend.level = level

        logger.log(level) { "message" }
        logger.logMethod("message")

        assertEquals(2, backend.events.size)
        assertEquals(level, backend.events[0].level)
        assertEquals(level, backend.events[1].level)
    }

    private fun testLevelFiltered(level: LogLevel, logMethod: ContextLogger.(String) -> Unit) = test {
        backend.level = null

        logger.log(level) { "message" }
        logger.logMethod("message")

        assertEquals(0, backend.events.size)
    }

    private fun test(block: TestContext.() -> Unit) {
        val backend = MockBackend()
        val logger = Logger(backend, NoDecorFactory).withoutContext()
        val context = TestContext(logger, backend)
        context.block()
    }

    private class TestContext(
        val logger: ContextLogger,
        val backend: MockBackend
    )

}
