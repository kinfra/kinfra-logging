package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * A logger that uses [LoggingContext] from [CoroutineContext] of the calling coroutine.
 *
 * An instance of [CoroutineLogger] can be obtained via [Logger.withCoroutineContext] method.
 */
class CoroutineLogger internal constructor(
    private val emptyContextLogger: Logger,
    private val backend: LoggerBackend,
    private val factory: LoggerFactory
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
        val context = coroutineContext
        if (isEnabled(level, context)) {
            val message = lazyMessage.invoke()
            log(level, message, error, context)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel, context: CoroutineContext): Boolean {
        val loggingContext = LoggingContext.fromCoroutineContext(context)
        return backend.isEnabled(level, loggingContext)
    }

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?, context: CoroutineContext) {
        val loggingContext = LoggingContext.fromCoroutineContext(context)
        backend.log(level, message, error, loggingContext, callerInfo)
    }

    /**
     * Returns a [Logger] for the same class with an empty context.
     *
     * @see Logger.withCoroutineContext
     */
    fun withoutContext(): Logger {
        return emptyContextLogger
    }

    /**
     * Returns a [Logger] for the same class that use specified [context].
     */
    fun withContext(context: LoggingContext): Logger {
        return emptyContextLogger.withContext(context)
    }

    override fun toString(): String {
        return "CoroutineLogger(backend: $backend)"
    }

    companion object {
        private val callerInfo = CallerInfo(Logger::class.java.name)
    }

}
