package ru.kontur.jinfra.logging.decor

import ru.kontur.jinfra.logging.LoggingContext

/**
 * Decorates a message to be logged with [context][LoggingContext] data.
 *
 * For each implementation there should be a single instance of empty decor - a decor for empty context.
 * Decor for a non-empty context is obtained via [plusElement] method.
 *
 * Implementation must be immutable.
 */
interface MessageDecor {

    /**
     * Decorate the [message].
     */
    fun decorate(message: String): String

    /**
     * Add an element to this decor.
     */
    fun plusElement(element: LoggingContext.Element): MessageDecor

    /**
     * A [MessageDecor] that does not decorate messages in any way.
     */
    object Nop : MessageDecor {

        override fun decorate(message: String) = message

        override fun plusElement(element: LoggingContext.Element) = this

    }

}
