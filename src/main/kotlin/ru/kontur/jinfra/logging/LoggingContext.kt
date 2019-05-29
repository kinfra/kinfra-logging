package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Contains supplementary data that should be logged with every log message.
 *
 * LoggingContext can be placed in [CoroutineContext] to propagate it inside entire call graph.
 * In that case you should use [CoroutineLogger] that will use that context:
 * ```
 * val logger: CoroutineLogger = Logger.currentClass().withCoroutineContext()
 * val userId = ...
 * withContext(LoggingContext.with("userId", userId)) {
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * Alternatively a LoggingContext [can be passed][Logger.withContext] to an instance of [Logger]
 * in order to use it in all messages logged by the instance:
 * ```
 * fun doSomething(..., logContext: LoggingContext) {
 *     val logger = logger.withContext(logContext)
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * These approaches can be combined: for example, a context can be captured in a suspending function
 * via [LoggingContext.current] and then used in a [Logger] via [Logger.withContext].
 *
 * New elements can be added to the context via [LoggingContext.add] and [LoggingContext.with] (in a coroutine).
 * The context is immutable, adding new elements creates a new context.
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

    private val prefix: String = buildString {
        for (element in elements) {
            append("[")
            append(element.value)
            append("] ")
        }
    }

    operator fun get(key: String): String? {
        return elements.find { it.key == key }?.value
    }

    /**
     * Returns a context composed from this context and an element with specified [key] and [value].
     *
     * This context must not contain an element with the same [key].
     *
     * @see Logger.addContext
     */
    fun add(key: String, value: Any): LoggingContext {
        val currentValue = get(key)
        require(currentValue == null) {
            "Context already contains an element with key '$key'" +
                    " (current value: '$currentValue', new value: '$value')"
        }

        return LoggingContext(
            _elements + Element(key, value.toString())
        )
    }

    /**
     * Render context data into a [message] supplied by [Logger]'s user.
     *
     * This method is for use in [LoggerBackend].
     */
    fun decorate(message: String): String {
        return prefix + message
    }

    override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoggingContext) return false

        return _elements == other._elements
    }

    override fun hashCode(): Int {
        return _elements.hashCode()
    }

    override fun toString(): String {
        return if (_elements.isEmpty()) {
            "LoggingContext(empty)"
        } else {
            "LoggingContext$_elements"
        }
    }

    class Element internal constructor(
        val key: String,
        val value: String
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Element) return false
            return key == other.key && value == other.value
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString(): String {
            return "$key=$value"
        }

    }

    companion object : CoroutineContext.Key<LoggingContext> {

        /**
         * Empty context. This context has no elements.
         */
        val EMPTY: LoggingContext = LoggingContext(emptyList())

        /**
         * Returns [LoggingContext] of the calling coroutine.
         */
        suspend inline fun current(): LoggingContext {
            return fromCoroutineContext(coroutineContext)
        }

        /**
         * Returns [LoggingContext] contained in a [CoroutineContext] or [EMPTY] if there is not one.
         */
        fun fromCoroutineContext(context: CoroutineContext): LoggingContext {
            return context[LoggingContext] ?: EMPTY
        }

        /**
         * A shortcut for `LoggingContext.current().add(key, value)`
         *
         * @see LoggingContext.current
         * @see LoggingContext.add
         */
        suspend inline fun with(key: String, value: Any): LoggingContext {
            return current().add(key, value)
        }

    }

}
