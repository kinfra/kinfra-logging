package ru.kontur.jinfra.logging.context

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.Logger
import ru.kontur.jinfra.logging.LoggerBackend
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ContextLogger(
    private val backend: LoggerBackend
) {

    val withoutContext = Logger(backend)

    suspend inline fun trace(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.TRACE, error, lazyMessage)
    }

    suspend inline fun debug(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    suspend inline fun info(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    suspend inline fun warn(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    suspend inline fun error(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    suspend inline fun log(level: LogLevel, error: Throwable? = null, lazyMessage: () -> String) {
        if (isEnabled(level)) {
            val message = lazyMessage.invoke()
            log(level, message, error, coroutineContext)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel): Boolean = backend.isEnabled(level)

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?, context: CoroutineContext) {
        backend.log(level, message, error, context[LoggingContext] ?: LoggingContext.EMPTY)
    }

    override fun toString(): String {
        return "ContextLogger(backend: $backend)"
    }

    companion object {

        fun backedBy(backend: LoggerBackend): ContextLogger = ContextLogger(backend)

    }

}
