package ru.kontur.jinfra.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import ru.kontur.jinfra.logging.decor.MessageDecor
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

class LoggingContextTests {

    @Test
    fun empty_does_not_have_elements() {
        val elements = LoggingContext.EMPTY.elements.toList()

        assertEquals(emptyList<LoggingContext.Element>(), elements)
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
    fun equals_same_elements() {
        val context1 = LoggingContext.EMPTY
            .add("foo", "bar")

        val context2 = LoggingContext.EMPTY
            .add("foo", "bar")

        assertEquals(context1, context2)
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
    fun decor_differently() {
        val context = LoggingContext.EMPTY
            .add("foo", "123")
            .add("bar", "456")

        val prefixed = context.decorate("message", KeyPrefixLoggerFactory)
        assertEquals("foo bar message", prefixed)

        val postfixed = context.decorate("message", KeyPostfixLoggerFactory)
        assertEquals("message foo bar", postfixed)
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
