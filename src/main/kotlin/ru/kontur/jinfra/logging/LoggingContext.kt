package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.LoggingContext.Element
import ru.kontur.jinfra.logging.decor.MessageDecor
import ru.kontur.jinfra.logging.impl.ContextElementSet
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Contains supplementary data that should be logged with every log message.
 *
 * Consists of an **ordered** set of [key-value pairs][Element] ([elements]).
 *
 * LoggingContext can be placed in [CoroutineContext] to propagate it inside entire call graph.
 * In that case you should use [Logger] that will use that context:
 * ```
 * val logger: Logger = Logger.currentClass()
 * val userId = ...
 * withContext(LoggingContext.with("userId", userId)) {
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * Alternatively a LoggingContext [can be passed][Logger.withContext] to an instance of [ContextLogger]
 * in order to use it for all messages logged by the instance:
 * ```
 * private val logger: Logger = Logger.currentClass()
 *
 * fun doSomething(..., logContext: LoggingContext) {
 *     val logger: ContextLogger = logger.withContext(logContext)
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * These approaches can be combined: for example, a context can be captured in a suspending function
 * via [LoggingContext.current] and then used in a [ContextLogger] via [Logger.withContext].
 *
 * New elements can be added to the context via [LoggingContext.add] and [LoggingContext.with] (in a coroutine).
 * The context is immutable, adding new elements creates a new context.
 */
sealed class LoggingContext : CoroutineContext.Element {
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
     * If no such element is found, returns `null`.
     */
    abstract operator fun get(key: String): String?

    /**
     * Returns a context composed from this context and an element with specified [key] and [value].
     *
     * This context must not contain an element with the same [key].
     *
     * @see ContextLogger.addContext
     */
    fun add(key: String, value: Any): LoggingContext {
        return Populated(this, Element(key, value.toString()))
    }

    /**
     * Renders context data into a [message] supplied by [Logger]'s user.
     */
    internal fun decorate(message: String, factory: LoggerFactory): String {
        return getDecor(factory).decorate(message)
    }

    protected abstract fun getDecor(factory: LoggerFactory): MessageDecor

    /**
     * Returns a map containing elements of this context.
     *
     * Iteration order of the map is the same as in [elements].
     */
    abstract fun asMap(): Map<String, String>

    /**
     * Returns `true` if the context is empty (contains no elements), `false` otherwise.
     */
    abstract fun isEmpty(): Boolean

    override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    @Deprecated("Logging contexts cannot be merged", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
    operator fun plus(other: LoggingContext): CoroutineContext {
        return other
    }

    private object Empty : LoggingContext() {

        override fun get(key: String): String? = null

        override fun getDecor(factory: LoggerFactory) = factory.getEmptyDecorInternal()

        override val elements: Iterable<Element> get() = emptyList()

        override fun asMap(): Map<String, String> = emptyMap()

        override fun isEmpty() = true

        override fun toString() = "(empty)"

    }

    private class Populated(
        private val parent: LoggingContext,
        private val element: Element
    ) : LoggingContext() {

        @Volatile
        private var _elements: ContextElementSet? = null

        @Volatile
        private var cachedDecor: CachedDecor? = null

        init {
            val currentValue = parent[element.key]
            require(currentValue == null) {
                "Context already contains an element with key '$key'" +
                        " (current value: '$currentValue', new value: '${element.value}')"
            }
        }

        override val elements: ContextElementSet
            get() = _elements ?: createElements().also {
                _elements = it
            }

        private fun createElements(): ContextElementSet {
            return when (parent) {
                is Empty -> ContextElementSet(element)
                is Populated -> ContextElementSet(parent.elements, element)
            }
        }

        override fun get(key: String): String? {
            var current = this
            while (true) {
                val currentElement = current.element
                if (currentElement.key == key) {
                    return currentElement.value
                }

                when (val next = current.parent) {
                    is Populated -> current = next
                    is Empty -> return null
                }
            }
        }

        override fun getDecor(factory: LoggerFactory): MessageDecor {
            val cachedDecor = this.cachedDecor
                ?.takeIf { it.factory === factory }

            return cachedDecor?.decor ?: parent.getDecor(factory).plusElement(element).also {
                this.cachedDecor = CachedDecor(it, factory)
            }
        }

        override fun asMap(): Map<String, String> {
            return elements.asMap
        }

        override fun isEmpty() = false

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Populated) return false

            return element == other.element && parent == other.parent
        }

        override fun hashCode(): Int {
            // This hashCode() is more "relaxed" than equals()
            return elements.hashCode()
        }

        override fun toString(): String {
            return elements.toString()
        }

        private class CachedDecor(
            val decor: MessageDecor,
            val factory: LoggerFactory
        )

    }

    /**
     * A key-value pair that represents an aspect of context in which some code is executed.
     * For example, identifier of a user, operation or request.
     */
    class Element internal constructor(
        override val key: String,
        override val value: String
    ) : Map.Entry<String, String> {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Element) return false
            return key == other.key && value == other.value
        }

        override fun hashCode() = 31 * key.hashCode() + value.hashCode()

        override fun toString() = "$key=$value"

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
