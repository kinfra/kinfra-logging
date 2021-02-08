package ru.kontur.kinfra.logging

import kotlinx.coroutines.ThreadContextElement
import ru.kontur.kinfra.logging.LoggingContext.Element
import ru.kontur.kinfra.logging.decor.MessageDecor
import ru.kontur.kinfra.logging.impl.ContextElementSet
import kotlin.coroutines.CoroutineContext

/**
 * Contains supplementary data that should be logged with every log message.
 *
 * Consists of an **ordered** set of [key-value pairs][Element] ([elements]).
 *
 * LoggingContext can be placed in [CoroutineContext] to propagate it inside entire call graph of a coroutine:
 * ```
 * val logger: Logger = Logger.currentClass()
 * val userId = ...
 * withContext(LoggingContext.with("userId", userId)) {
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * The same can be done in a non-`suspend` function as well via [withLoggingContext] method:
 * ```
 * val logger: Logger = Logger.currentClass()
 * val userId = ...
 * withLoggingContext("userId", userId) {
 *     logger.info { "Log message" }
 *     ...
 * }
 * ```
 *
 * New elements can be added to the context via [LoggingContext.with].
 *
 * The context is immutable, adding new elements creates a new context.
 */
sealed class LoggingContext : CoroutineContext.Element {

    final override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    /**
     * Elements contained in this context.
     */
    abstract val elements: Collection<Element>

    /**
     * Returns [value][Element.value] of the context element having specified key.
     * If no such element is found, returns `null`.
     */
    abstract operator fun get(key: String): String?

    /**
     * Obtains a decor instance based on specified [empty] decor to render data of this context.
     */
    internal abstract fun getDecor(empty: MessageDecor): MessageDecor

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

    /**
     * Returns a context composed of this context and an element with specified [key] and [value].
     *
     * This context must not contain an element with the same [key].
     */
    fun with(key: String, value: Any): LoggingContext {
        val element = Element(key, value.toString())
        return PopulatedContext(this, element)
    }

    @Deprecated("Logging contexts cannot be merged", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
    operator fun plus(other: LoggingContext): CoroutineContext {
        return other
    }

    /**
     * A key-value pair that represents an aspect of context in which some code is executed.
     * For example, identifier of a user, operation or request.
     */
    class Element(
        override val key: String,
        override val value: String
    ) : Map.Entry<String, String> {

        init {
            require(key.isNotEmpty()) { "Key must not be empty" }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Element) return false
            return key == other.key && value == other.value
        }

        override fun hashCode() = 31 * key.hashCode() + value.hashCode()

        override fun toString(): String {
            return if (value.isNotEmpty()) "$key=$value" else key
        }

    }

    private object EmptyContext : LoggingContext(), ThreadContextElement<LoggingContext> {
        override fun get(key: String): String? = null
        override fun getDecor(empty: MessageDecor) = empty
        override val elements: Collection<Element> get() = emptyList()
        override fun asMap(): Map<String, String> = emptyMap()
        override fun isEmpty() = true
        override fun updateThreadContext(context: CoroutineContext): LoggingContext {
            return replaceContext(fromCoroutineContext(context))
        }
        override fun restoreThreadContext(context: CoroutineContext, oldState: LoggingContext) {
            restoreContext(oldState)
        }
        override fun toString() = "(empty)"
    }

    internal class PopulatedContext internal constructor(
        private val parent: LoggingContext,
        private val element: Element
    ) : LoggingContext(),
        ThreadContextElement<LoggingContext> {

        @Volatile
        private var allElements: ContextElementSet? = null

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
            get() = allElements ?: createElementSet().also {
                allElements = it
            }

        private fun createElementSet(): ContextElementSet {
            return when (parent) {
                is EmptyContext -> ContextElementSet(element)
                is PopulatedContext -> ContextElementSet(parent.elements, element)
            }
        }

        override fun get(key: String): String? {
            return if (element.key == key) {
                element.value
            } else {
                parent[key]
            }
        }

        override fun getDecor(empty: MessageDecor): MessageDecor {
            val cachedDecor = this.cachedDecor
                ?.takeIf { it.empty == empty }

            return cachedDecor?.filled ?: parent.getDecor(empty).plusElement(element).also {
                this.cachedDecor = CachedDecor(it, empty)
            }
        }

        override fun asMap(): Map<String, String> {
            return elements.asMap
        }

        override fun isEmpty() = false

        override fun updateThreadContext(context: CoroutineContext): LoggingContext {
            return replaceContext(fromCoroutineContext(context))
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: LoggingContext) {
            restoreContext(oldState)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PopulatedContext) return false

            return element == other.element && parent == other.parent
        }

        override fun hashCode(): Int {
            return element.hashCode() + parent.hashCode()
        }

        override fun toString(): String {
            return elements.toString()
        }

        private class CachedDecor(
            val filled: MessageDecor,
            val empty: MessageDecor
        )

    }

    companion object : CoroutineContext.Key<LoggingContext> {

        /**
         * Empty context. This context has no elements.
         */
        @JvmField
        val EMPTY: LoggingContext = EmptyContext

        private val threadLocal = ThreadLocal<LoggingContext>()

        /**
         * Returns [LoggingContext] of the current thread or coroutine.
         */
        fun current(): LoggingContext {
            return threadLocal.get() ?: EMPTY
        }

        internal fun setCurrent(context: LoggingContext) {
            threadLocal.set(context)
        }

        /**
         * Returns [LoggingContext] contained in a [CoroutineContext] or [EMPTY] if there is not one.
         */
        fun fromCoroutineContext(context: CoroutineContext): LoggingContext {
            return context[LoggingContext] ?: EMPTY
        }

        /**
         * A shortcut for `LoggingContext.current().with(key, value)`
         *
         * @see LoggingContext.current
         * @see LoggingContext.with
         */
        fun with(key: String, value: Any): LoggingContext {
            return current().with(key, value)
        }

    }

}

/**
 * Run [block] of code in a logging context with an element with specified [key] and [value].
 *
 * In suspending code, use `withContext(LoggingContext.with(key,value)) { ... }` instead.
 */
// crossinline disallows suspending
inline fun <R> withLoggingContext(key: String, value: Any, crossinline block: () -> R): R {
    val oldContext = addContext(key, value)
    return try {
        block()
    } finally {
        restoreContext(oldContext)
    }
}

/**
 * Run [block] of code in specified logging [context].
 *
 * In suspending code, use `withContext(LoggingContext.with(key,value)) { ... }` instead.
 */
// crossinline disallows suspending
inline fun <R> withLoggingContext(context: LoggingContext, crossinline block: () -> R): R {
    val oldContext = replaceContext(context)
    return try {
        block()
    } finally {
        restoreContext(oldContext)
    }
}

@PublishedApi
internal fun addContext(key: String, value: Any): LoggingContext {
    return LoggingContext.current().also {
        LoggingContext.setCurrent(it.with(key, value))
    }
}

@PublishedApi
internal fun replaceContext(newContext: LoggingContext): LoggingContext {
    return LoggingContext.current().also {
        LoggingContext.setCurrent(newContext)
    }
}

@PublishedApi
internal fun restoreContext(oldContext: LoggingContext) {
    LoggingContext.setCurrent(oldContext)
}
