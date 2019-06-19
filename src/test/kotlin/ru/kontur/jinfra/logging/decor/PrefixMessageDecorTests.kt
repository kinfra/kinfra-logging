package ru.kontur.jinfra.logging.decor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.kontur.jinfra.logging.LoggingContext

class PrefixMessageDecorTests {

    @Test
    fun empty_is_empty() {
        val decor = PrefixMessageDecor.EMPTY

        assertEquals("message", decor.decorate("message"))
    }

    @Test
    fun decor_one_element() {
        val decor = PrefixMessageDecor.EMPTY
            .plusElement(LoggingContext.Element("foo", "123"))

        assertEquals("[123] message", decor.decorate("message"))
    }

    @Test
    fun decor_two_elements() {
        val decor = PrefixMessageDecor.EMPTY
            .plusElement(LoggingContext.Element("foo", "123"))
            .plusElement(LoggingContext.Element("bar", "456"))

        assertEquals("[123] [456] message", decor.decorate("message"))
    }

}