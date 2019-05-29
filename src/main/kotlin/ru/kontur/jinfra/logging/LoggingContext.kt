package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Contains supplementary data that should be logged with every log message.
 *
 * Consists of an **ordered** set of [key-value pairs][Element] ([elements]).
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
abstract class LoggingContext private constructor() : CoroutineContext.Element {
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

    /**
     * Elements contained in this context.
     */
    abstract val elements: Iterable<Element>

    /**
     * Returns [value][Element.value] of the context element having specified key.
     * If no such element is found, returns null.
     */
    abstract operator fun get(key: String): String?

    /**
     * Returns a context composed from this context and an element with specified [key] and [value].
     *
     * This context must not contain an element with the same [key].
     *
     * @see Logger.addContext
     */
    fun add(key: String, value: Any): LoggingContext {
        return Populated(this, Element(key, value.toString()))
    }

    /**
     * Render context data into a [message] supplied by [Logger]'s user.
     *
     * This method is for use in [LoggerBackend].
     */
    internal fun decorate(message: String, factory: LoggerFactory): String {
        return getDecor(factory).decorate(message)
    }

    protected abstract fun getDecor(factory: LoggerFactory): MessageDecor

    override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    private object Empty : LoggingContext() {

        override fun get(key: String): String? = null

        override fun getDecor(factory: LoggerFactory) = factory.getEmptyDecor()

        override val elements: Iterable<Element> get() = emptyList()

        override fun equals(other: Any?) = other is Empty

        override fun hashCode() = 0

        override fun toString() = "LoggingContext (empty)"

    }

    private class Populated(
        private val parent: LoggingContext,
        private val element: Element
    ) : LoggingContext() {

        // todo: optimize elements (don't use List)
        private val _elements: List<Element>

        @Volatile
        private var cachedDecor: CachedDecor? = null

        init {
            val currentValue = parent[element.key]
            require(currentValue == null) {
                "Context already contains an element with key '$key'" +
                        " (current value: '$currentValue', new value: '${element.value}')"
            }

            this._elements = parent.elements + element
        }

        override fun get(key: String): String? {
            return _elements.find { it.key == key }?.value
        }

        override fun getDecor(factory: LoggerFactory): MessageDecor {
            val cachedDecor = this.cachedDecor?.takeIf { it.factory === factory }
            return if (cachedDecor != null) {
                cachedDecor.decor
            } else {
                val decor = parent.getDecor(factory).plusElement(element)
                this.cachedDecor = CachedDecor(decor, factory)
                decor
            }
        }

        override val elements: Iterable<Element>
            get() = _elements

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Populated) return false

            return _elements == other._elements
        }

        override fun hashCode(): Int {
            return _elements.hashCode()
        }

        override fun toString(): String {
            return "LoggingContext$_elements"
        }

        private class CachedDecor(
            val decor: MessageDecor,
            val factory: LoggerFactory
        )

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
        val EMPTY: LoggingContext = Empty

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
