package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * An object used to log messages for a specified class.
 *
 * Loggers can be obtained from a [LoggerFactory].
 * There are convenient extensions to obtain a logger from the [default factory][DefaultLoggerFactory]:
 *
 *  * [Logger.Companion.forClass] to obtain a logger for use in specified class.
 *
 *  * [Logger.Companion.currentClass] to obtain a logger for use in current class,
 *    i.e. in the class calling that method.
 *
 * All logging methods are `suspend`. It allows them to implicitly use [LoggingContext] of the calling coroutine.
 * If you dont need this **and** you need a logger for non-`suspend` code consider using a [ContextLogger]
 * with empty context. It can be obtained via [withoutContext].
 *
 * @see Logger.Companion.currentClass
 * @see Logger.Companion.forClass
 * @see LoggingContext
 * @see ContextLogger
 */
class Logger internal constructor(
    private val backend: LoggerBackend,
    private val factory: LoggerFactory
) {

    /**
     * For a given LoggerBackend at most one ContextLogger with empty context needs to be constructed.
     * This field refers that logger.
     */
    @Volatile
    private var emptyContextLogger: ContextLogger? = null

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
        val decoratedMessage = loggingContext.decorate(message, factory)
        backend.log(level, decoratedMessage, error, loggingContext, callerInfo)
    }

    /**
     * Returns a [ContextLogger] for the same class with an empty context.
     */
    fun withoutContext(): ContextLogger {
        return this.emptyContextLogger ?: ContextLogger(LoggingContext.EMPTY, this, backend, factory).also {
            this.emptyContextLogger = it
        }
    }

    /**
     * Returns a [ContextLogger] for the same class that use specified [context].
     */
    fun withContext(context: LoggingContext): ContextLogger {
        return if (context == LoggingContext.EMPTY) {
            withoutContext()
        } else {
            ContextLogger(context, this, backend, factory)
        }
    }

    override fun toString(): String {
        return "Logger(backend: $backend)"
    }

    companion object {

        private val callerInfo = CallerInfo(Logger::class.java.name)

    }

}
