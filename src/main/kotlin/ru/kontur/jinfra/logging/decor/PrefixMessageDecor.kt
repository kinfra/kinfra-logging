package ru.kontur.jinfra.logging.decor

import ru.kontur.jinfra.logging.LoggingContext

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
