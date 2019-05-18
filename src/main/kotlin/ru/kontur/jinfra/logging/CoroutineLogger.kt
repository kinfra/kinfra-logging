package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * A logger that uses [LoggingContext] from [CoroutineContext] of the calling coroutine.
 *
 * An instance of [CoroutineLogger] can be obtained via [Logger.withCoroutineContext] method.
 */
class CoroutineLogger internal constructor(
    private val backend: LoggerBackend
) {

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

    /**
     * Returns a [Logger] backed by the same [LoggerBackend] with an empty context.
     *
     * @see Logger.withCoroutineContext
     */
    fun withoutContext(): Logger {
        return Logger(backend, LoggingContext.EMPTY)
    }

    /**
     * Returns a [Logger] backed by the same [LoggerBackend] that use specified [context].
     */
    fun withContext(context: LoggingContext): Logger {
        return Logger(backend, context)
    }

    override fun toString(): String {
        return "CoroutineLogger(backend: $backend)"
    }

}
