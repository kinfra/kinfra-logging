package ru.kontur.jinfra.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.test.MockBackend

class ContextLoggerTests {

    @Test
    fun level_trace() = test {
        backend.level = LogLevel.TRACE

        logger.trace { "message" }
        logger.log(LogLevel.TRACE) { "message" }

        assertEquals(2, backend.events.size)
        assertEquals(LogLevel.TRACE, backend.events[0].level)
        assertEquals(LogLevel.TRACE, backend.events[1].level)
    }

    @Test
    fun level_trace_filtered() = test {
        backend.level = LogLevel.DEBUG

        logger.trace { "log message" }

        assertEquals(0, backend.events.size)
    }

    private fun test(block: TestContext.() -> Unit) {
        val backend = MockBackend()
        val logger = Logger(backend, DefaultLoggerFactory).withoutContext()
        val context = TestContext(logger, backend)
        context.block()
    }

    private class TestContext(
        val logger: ContextLogger,
        val backend: MockBackend
    )

}
