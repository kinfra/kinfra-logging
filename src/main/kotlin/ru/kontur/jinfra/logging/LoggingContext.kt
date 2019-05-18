package ru.kontur.jinfra.logging

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Contains supplementary data that should be logged with every log message.
 *
 * [LoggingContext] can be placed in [CoroutineContext] to propagate it inside entire call graph.
 * In that case you should use [CoroutineLogger] that will use that context.
 *
 * Alternatively a [LoggingContext] can be passed to an instance of [Logger]
 * to use in all messages logged by that instance.
 *
 * These approaches can be combined: for example, context can be captured in a suspending function
 * via [LoggingContext.current] and then used in a [Logger] via [Logger.withContext].
 *
 * New elements can be added to the context via [LoggingContext.with] (in a coroutine) and [Logger.addContext].
 */
class LoggingContext private constructor(
    elements: List<Element>
) : CoroutineContext.Element {
    /*
     * A note on public API:
     * Beware that exposing a method returning a new LoggingContext
     * can lead to its misuse in CoroutineScope.withContext().
     * For example suppose there is a LoggingContext.of(key, value) method.
     * A user may call it like this:
     *
     *   withContext(LoggingContext.of("key", "value")) { ... }
     *
     * This way current context will be lost (it will be replaced by the new context).
     * So it's better to avoid having such API.
     */

    // todo: optimize elements and prefix construction (don't use List)
    private val _elements: List<Element> = elements

    val elements: Iterable<Element>
        get() = _elements

    val prefix: String = buildString {
        for (element in elements) {
            append("[")
            append(element.value)
            append("] ")
        }
    }

    operator fun get(key: String): String? {
        return elements.find { it.key == key }?.value
    }

    internal fun plus(key: String, value: String): LoggingContext {
        val currentValue = get(key)
        require(currentValue == null) {
            "Context already contains an element with key '$key'" +
                    " (current value: '$currentValue', new value: '$value')"
        }

        return LoggingContext(
            _elements + Element(
                key,
                value
            )
        )
    }

    override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    override fun toString(): String {
        return "LoggingContext$_elements"
    }

    class Element internal constructor(
        val key: String,
        val value: String
    )

    companion object : CoroutineContext.Key<LoggingContext> {

        val EMPTY: LoggingContext = LoggingContext(emptyList())

        /**
         * Returns [LoggingContext] of the calling coroutine.
         */
        suspend inline fun current(): LoggingContext {
            return currentImpl(coroutineContext)
        }

        @PublishedApi
        internal fun currentImpl(context: CoroutineContext): LoggingContext {
            return context[LoggingContext] ?: EMPTY
        }

        /**
         * Returns a context composed from the context of the calling coroutine
         * and an element with specified [key] and [value].
         *
         * Current context must not contain an element with the same [key].
         *
         * @see Logger.addContext
         */
        suspend inline fun with(key: String, value: Any): LoggingContext {
            return withImpl(coroutineContext, key, value)
        }

        @PublishedApi
        internal fun withImpl(context: CoroutineContext, key: String, value: Any): LoggingContext {
            return currentImpl(context).plus(key, value.toString())
        }

    }

}