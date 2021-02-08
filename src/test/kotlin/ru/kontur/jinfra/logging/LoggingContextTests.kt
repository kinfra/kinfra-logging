package ru.kontur.jinfra.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import ru.kontur.jinfra.logging.decor.MessageDecor
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.Delegates
import kotlin.reflect.KClass

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
    fun current() {
        val empty = LoggingContext.EMPTY
        val context = empty.with("foo", "bar")

        assertEquals(empty, LoggingContext.current())

        withLoggingContext(context) {
            assertEquals(context, LoggingContext.current())
        }

        assertEquals(empty, LoggingContext.current())
    }

    @Test
    // todo: enable after release of kotlinx-coroutines 1.4.3
    @Disabled("broken because of kotlinx.coroutines bug")
    fun current_in_coroutine() {
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
            }

            // Should be empty outside withContext { }
            assertEquals(empty, LoggingContext.current())
        }
    }

    @Test
    fun with_logging_key_value() {
        withLoggingContext("key", "value") {
            val element = LoggingContext.current().elements.single()
            assertEquals("key", element.key)
            assertEquals("value", element.value)
        }
    }

    @Test
    fun from_coroutine_context() {
        val expected = LoggingContext.EMPTY.with("foo", "bar")

        val actual = runBlocking(expected + Dispatchers.Default) {
            LoggingContext.fromCoroutineContext(coroutineContext)
        }

        assertEquals(expected, actual)
    }

    @Test
    fun from_coroutine_context_empty() {
        assertEquals(LoggingContext.EMPTY, LoggingContext.fromCoroutineContext(EmptyCoroutineContext))
    }

    @Test
    fun with_element() {
        val context = runBlocking(LoggingContext.EMPTY.with("foo", "bar")) {
            withContext(LoggingContext.with("bar", "baz")) {
                LoggingContext.current()
            }
        }

        val elements = context.elements.toList()
        assertEquals(2, elements.size)
        assertEquals("foo", elements[0].key)
        assertEquals("bar", elements[1].key)
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
    }

    private object DelegatingLoggerFactory : LoggerFactory.Wrapper() {
        public override var delegate: LoggerFactory by Delegates.notNull()
    }

    private object KeyPrefixLoggerFactory : LoggerFactory() {

        override fun getEmptyDecor(): MessageDecor {
            return Decor("")
        }

        override fun getLoggerBackend(kClass: KClass<*>) = fail { "should not be called" }

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

        override fun getLoggerBackend(kClass: KClass<*>) = fail { "should not be called" }

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
