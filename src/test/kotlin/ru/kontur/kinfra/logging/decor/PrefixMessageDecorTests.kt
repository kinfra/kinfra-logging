package ru.kontur.kinfra.logging.decor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.kinfra.logging.LoggingContext

class PrefixMessageDecorTests {

    @Test
    fun empty_is_empty() {
        val decor = DefaultMessageDecor.EMPTY

        assertEquals("message", decor.decorate("message"))
    }

    @Test
    fun decor_one_element() {
        val decor = DefaultMessageDecor.EMPTY
            .plusElement(LoggingContext.Element("foo", "123"))

        assertEquals("[123] message", decor.decorate("message"))
    }

    @Test
    fun decor_two_elements() {
        val decor = DefaultMessageDecor.EMPTY
            .plusElement(LoggingContext.Element("foo", "123"))
            .plusElement(LoggingContext.Element("bar", "456"))

        assertEquals("[123] [456] message", decor.decorate("message"))
    }

    @Test
    fun decor_empty_element() {
        val decor = DefaultMessageDecor.EMPTY
            .plusElement(LoggingContext.Element("foo", ""))
            .plusElement(LoggingContext.Element("bar", "123"))

        assertEquals("[123] message", decor.decorate("message"))
    }

}
