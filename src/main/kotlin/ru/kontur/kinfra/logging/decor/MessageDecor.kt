package ru.kontur.kinfra.logging.decor

import ru.kontur.kinfra.logging.LoggingContext

/**
 * Decorates a message to be logged with [context][LoggingContext] data.
 *
 * For each implementation there should be a single instance of empty decor - a decor for empty context.
 * Decor for a non-empty context is obtained via [plusElement] method.
 *
 * Implementation must be immutable.
 */
public interface MessageDecor {

    /**
     * Decorate the [message].
     */
    public fun decorate(message: String): String

    /**
     * Add an element to this decor.
     */
    public fun plusElement(element: LoggingContext.Element): MessageDecor

    /**
     * A [MessageDecor] that does not decorate messages in any way.
     */
    public object Nop : MessageDecor {

        override fun decorate(message: String): String = message

        override fun plusElement(element: LoggingContext.Element): Nop = this

    }

}
