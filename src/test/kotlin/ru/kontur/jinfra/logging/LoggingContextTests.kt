package ru.kontur.jinfra.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import ru.kontur.jinfra.logging.decor.MessageDecor
import kotlin.coroutines.EmptyCoroutineContext
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
        val context = LoggingContext.EMPTY.add("foo", "bar")

        val elements = context.elements.toList()
        assertFalse(context.isEmpty())
        assertEquals(1, elements.size)
        assertEquals("foo", elements[0].key)
        assertEquals("bar", elements[0].value)
    }

    @Test
    fun current() {
        val expected = LoggingContext.EMPTY.add("foo", "bar")

        val actual = runBlocking(expected) {
            LoggingContext.current()
        }

        assertEquals(expected, actual)
    }

    @Test
    fun from_coroutine_context() {
        val expected = LoggingContext.EMPTY.add("foo", "bar")

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
        val context = runBlocking(LoggingContext.EMPTY.add("foo", "bar")) {
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
            .add("foo", "123")
            .add("bar", "456")
            .add("baz", "789")

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
            .add("foo", "123")
            .add("bar", "456")

        val context2 = LoggingContext.EMPTY
            .add("foo", "123")
            .add("bar", "456")

        assertEquals(context1, context2)
        assertEquals(context1.hashCode(), context2.hashCode())
    }

    @Test
    fun replace_prohibited() {
        assertThrows<IllegalArgumentException> {
            LoggingContext.EMPTY
                .add("foo", "bar")
                .add("foo", "baz")
        }
    }

    @Test
    fun empty_key_prohibited() {
        assertThrows<IllegalArgumentException> {
            LoggingContext.EMPTY.add("", "foo")
        }
    }

    @Test
    fun empty_value_allowed() {
        val context = LoggingContext.EMPTY.add("foo", "")
        assertEquals("", context["foo"])
    }

    @Test
    fun decor_differently() {
        val context = LoggingContext.EMPTY
            .add("foo", "123")
            .add("bar", "456")

        val prefixed = context.decorate("message", KeyPrefixLoggerFactory)
        assertEquals("foo bar message", prefixed)

        val postfixed = context.decorate("message", KeyPostfixLoggerFactory)
        assertEquals("message foo bar", postfixed)
    }

    @Test
    fun as_map() {
        val context = LoggingContext.EMPTY
            .add("foo", "123")
            .add("bar", "456")

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
