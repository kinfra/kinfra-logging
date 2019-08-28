package ru.kontur.jinfra.logging.decor

import ru.kontur.jinfra.logging.LoggingContext

/**
 * Decor that prefixes log messages with values of context elements.
 *
 * For example a message "Hi!" in context `[abc=123,def=456]` will be decorated as follows:
 * ```
 * [123] [456] Hi!
 * ```
 *
 * Context elements with empty values are ignored.
 */
internal class PrefixMessageDecor private constructor(
    private val prefix: String
) : MessageDecor {

    override fun decorate(message: String): String {
        return prefix + message
    }

    override fun plusElement(element: LoggingContext.Element): MessageDecor {
        val value = element.value

        return if (value.isNotEmpty()) {
            PrefixMessageDecor("$prefix[$value] ")
        } else {
            this
        }
    }

    companion object {

        val EMPTY = PrefixMessageDecor("")

    }

}
