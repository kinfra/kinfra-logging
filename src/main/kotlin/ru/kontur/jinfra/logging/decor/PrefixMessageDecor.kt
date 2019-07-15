package ru.kontur.jinfra.logging.decor

import ru.kontur.jinfra.logging.LoggingContext

/**
 * Decor that prefixes log messages with values of context elements.
 *
 * For example a message "Hi!" in context `[abc=123,def=456]` will be decorated as follows:
 * ```
 * [123] [456] Hi!
 * ```
 */
internal class PrefixMessageDecor private constructor(
    private val prefix: String
) : MessageDecor {

    override fun decorate(message: String): String {
        return prefix + message
    }

    override fun plusElement(element: LoggingContext.Element): MessageDecor {
        return PrefixMessageDecor("$prefix[${element.value}] ")
    }

    companion object {

        val EMPTY = PrefixMessageDecor("")

    }

}
