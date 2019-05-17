package ru.kontur.jinfra.logging.context

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class LoggingContext private constructor(
    elements: List<Element>
) : CoroutineContext.Element {

    // todo: optimize elements and prefix (don't use List)
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

    private fun plus(key: String, value: String): LoggingContext {
        val currentValue = get(key)
        require(currentValue == null) {
            "Context already contains an element with key '$key'" +
                    " (current value: '$currentValue', new value: '$value')"
        }

        return LoggingContext(_elements + Element(key, value))
    }

    override val key: CoroutineContext.Key<*>
        get() = LoggingContext

    class Element internal constructor(
        val key: String,
        val value: String
    )

    companion object : CoroutineContext.Key<LoggingContext> {

        val EMPTY: LoggingContext = LoggingContext(emptyList())

        suspend inline fun with(key: String, value: Any): LoggingContext {
            return withImpl(coroutineContext, key, value)
        }

        @PublishedApi
        internal fun withImpl(currentContext: CoroutineContext, key: String, value: Any): LoggingContext {
            return (currentContext[LoggingContext] ?: EMPTY).plus(key, value.toString())
        }

    }

}
