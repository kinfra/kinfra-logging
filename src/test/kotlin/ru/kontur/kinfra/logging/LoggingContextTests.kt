package ru.kontur.kinfra.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import ru.kontur.kinfra.logging.backend.LoggerBackend
import ru.kontur.kinfra.logging.decor.MessageDecor
import kotlin.properties.Delegates

class LoggingContextTests {

    @Test
    fun empty_does_not_have_elements() {
        val context = LoggingContext.EMPTY

        assertTrue(context.isEmpty())
        assertTrue(context.elements.toList().isEmpty())
    }

    @Test
    fun populated_is_not_empty() {
        val context = LoggingContext.EMPTY.with("foo", "bar")

        val elements = context.elements.toList()
        assertFalse(context.isEmpty())
        assertEquals(1, elements.size)
        assertEquals("foo", elements[0].key)
        assertEquals("bar", elements[0].value)
    }

    @Test
    fun with_logging_context() {
        val empty = LoggingContext.EMPTY
        val sample = empty.with("foo", "bar")

        assertEquals(empty, LoggingContext.current())
        withLoggingContext(sample) {
            assertEquals(sample, LoggingContext.current())
        }
        assertEquals(empty, LoggingContext.current())
    }

    @Test
    fun with_logging_context_key_value() {
        val empty = LoggingContext.EMPTY
        val expected = empty.with("key", "value")

        assertEquals(empty, LoggingContext.current())
        withLoggingContext("key", "value") {
            assertEquals(expected, LoggingContext.current())
        }
        assertEquals(empty, LoggingContext.current())
    }

    @Test
    fun with_coroutine_context() {
        val empty = LoggingContext.EMPTY
        val context = empty.with("foo", "bar")

        runBlocking {
            // Should be empty by default
            assertEquals(empty, LoggingContext.current())

            // Run coroutine in the context
            withContext(context) {
                // In the same thread
                assertEquals(context, LoggingContext.current())

                withContext(Dispatchers.IO) {
                    // In a thread of Dispatchers.IO pool
                    assertEquals(context, LoggingContext.current())
                }

                // In the main thread again
                assertEquals(context, LoggingContext.current())
            }

            // Should be empty outside withContext { }
            assertEquals(empty, LoggingContext.current())
        }
    }

    @Test
    fun with_context_combined() {
        lateinit var outerContextBefore: LoggingContext
        lateinit var outerContextAfter: LoggingContext
        lateinit var innerContext: LoggingContext
        runBlocking(LoggingContext.with("foo", "123")) {
            outerContextBefore = LoggingContext.current()
            withContext(LoggingContext.with("bar", "456")) {
                innerContext = LoggingContext.current()
            }
            outerContextAfter = LoggingContext.current()
        }

        val expectedOuter = LoggingContext.EMPTY.with("foo", "123")
        val expectedInner = expectedOuter.with("bar", "456")
        assertAll(
            { assertEquals(expectedOuter, outerContextBefore) },
            { assertEquals(expectedInner, innerContext) },
            { assertEquals(expectedOuter, outerContextAfter) },
        )
    }

    @Test
    fun with_context_run_blocking() {
        withLoggingContext("foo", "123") {
            runBlocking {
                val context = LoggingContext.current()
                val expected = LoggingContext.EMPTY.with("foo", "123")
                assertEquals(expected, context)
            }
        }
    }

    @Test
    fun with_context_run_blocking_another_thread() {
        withLoggingContext("foo", "123") {
            runBlocking(Dispatchers.IO) {
                assertEquals(LoggingContext.EMPTY, LoggingContext.current())
            }
        }
    }

    @Test
    fun with_context_run_blocking_passed_explicitly() {
        withLoggingContext("foo", "123") {
            runBlocking(Dispatchers.IO + LoggingContext.current()) {
                val context = LoggingContext.current()
                val expected = LoggingContext.EMPTY.with("foo", "123")
                assertEquals(expected, context)
            }
        }
    }

    @Test
    fun get() {
        val context = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")
            .with("baz", "789")

        assertEquals("123", context["foo"])
        assertEquals("456", context["bar"])
        assertEquals("789", context["baz"])
        assertNull(context["missing"])
    }

    @Test
    fun get_empty() {
        assertNull(LoggingContext.EMPTY["foo"])
    }

    @Test
    fun equals_same_elements() {
        val context1 = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        val context2 = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        assertEquals(context1, context2)
        assertEquals(context1.hashCode(), context2.hashCode())
    }

    @Test
    fun equals_check_order() {
        val context1 = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        val context2 = LoggingContext.EMPTY
            .with("bar", "456")
            .with("foo", "123")

        assertNotEquals(context1, context2)
    }

    @Test
    fun replace_prohibited() {
        assertThrows<IllegalArgumentException> {
            LoggingContext.EMPTY
                .with("foo", "bar")
                .with("foo", "baz")
        }
    }

    @Test
    fun empty_key_prohibited() {
        assertThrows<IllegalArgumentException> {
            LoggingContext.EMPTY.with("", "foo")
        }
    }

    @Test
    fun empty_value_allowed() {
        val context = LoggingContext.EMPTY.with("foo", "")
        assertEquals("", context["foo"])
    }

    @Test
    fun decor_differently() {
        val context = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        val prefixed = context.decorate("message", KeyPrefixLoggerFactory)
        assertEquals("foo bar message", prefixed)

        val postfixed = context.decorate("message", KeyPostfixLoggerFactory)
        assertEquals("message foo bar", postfixed)
    }

    @Test
    fun decor_differently_same_factory() {
        val factory = DelegatingLoggerFactory
        val context = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        factory.delegate = KeyPrefixLoggerFactory
        val prefixed = context.decorate("message", factory)
        assertEquals("foo bar message", prefixed)

        factory.delegate = KeyPostfixLoggerFactory
        val postfixed = context.decorate("message", factory)
        assertEquals("message foo bar", postfixed)
    }

    private fun LoggingContext.decorate(message: String, factory: LoggerFactory): String {
        return getDecor(factory.getEmptyDecorInternal()).decorate(message)
    }

    @Test
    fun as_map() {
        val context = LoggingContext.EMPTY
            .with("foo", "123")
            .with("bar", "456")

        // reverse order is intended
        val expected = mapOf(
            "bar" to "456",
            "foo" to "123"
        )

        val map = context.asMap()
        assertEquals(expected, map)
        assertEquals(expected.entries, map.entries)
        assertTrue(map.containsKey("foo"))
        assertFalse(map.containsKey("baz"))
        assertTrue(map.containsValue("456"))
        assertFalse(map.containsValue("000"))
        assertEquals("123", map["foo"])
        assertEquals("456", map["bar"])
    }

    @Test
    fun withLoggingContext_has_callsInPlace_contract() {
        val foo: String
        withLoggingContext("key", "value") {
            foo = "bar"
        }
        assertEquals("bar", foo)
    }

    private object DelegatingLoggerFactory : LoggerFactory.Wrapper() {
        public override var delegate: LoggerFactory by Delegates.notNull()
    }

    private object KeyPrefixLoggerFactory : LoggerFactory() {

        override fun getEmptyDecor(): MessageDecor {
            return Decor("")
        }

        override fun getLoggerBackend(name: String): LoggerBackend = fail { "should not be called" }

        private class Decor(val value: String) : MessageDecor {
            override fun decorate(message: String): String {
                return value + message
            }

            override fun plusElement(element: LoggingContext.Element): MessageDecor {
                return Decor("$value${element.key} ")
            }
        }

    }

    private object KeyPostfixLoggerFactory : LoggerFactory() {

        override fun getEmptyDecor(): MessageDecor {
            return Decor("")
        }

        override fun getLoggerBackend(name: String): LoggerBackend = fail { "should not be called" }

        private class Decor(val value: String) : MessageDecor {
            override fun decorate(message: String): String {
                return message + value
            }

            override fun plusElement(element: LoggingContext.Element): MessageDecor {
                return Decor("$value ${element.key}")
            }
        }

    }

}
