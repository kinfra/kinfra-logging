package ru.kontur.jinfra.logging

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.test.MockBackend

class CoroutineLoggerTests {

    @Test
    fun context_logged() = test {
        withContext(LoggingContext.with("userId", 123)) {
            logger.info { "message" }
        }

        assertEquals(1, backend.events.size)
        with(backend.events[0]) {
            assertEquals("123", context["userId"])
        }
    }

    private fun test(block: suspend TestContext.() -> Unit) {
        val backend = MockBackend()
        val logger = Logger.backedBy(backend).withCoroutineContext()
        val context = TestContext(logger, backend)

        runBlocking {
            context.block()
        }
    }

    private class TestContext(
        val logger: CoroutineLogger,
        val backend: MockBackend
    )

}