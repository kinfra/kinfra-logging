package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend

/**
 * A logger with a fixed [context] attached to it.
 *
 * Use-cases for ContextLogger are:
 *
 *  * You dont care about context at all and need logger methods to be non-`suspend`.
 *
 *    In that case just use [Logger.withoutContext].
 *
 *  * You have a [LoggingContext] passed from somewhere and need to make use of it.
 *
 *    In that case you can use [Logger.withContext] or [ContextLogger.withContext].
 *
 * @see Logger
 * @see LoggingContext
 */
class ContextLogger internal constructor(
    val context: LoggingContext,
    private val logger: Logger,
    private val backend: LoggerBackend,
    private val factory: LoggerFactory
) {

    inline fun trace(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.TRACE, error, lazyMessage)
    }

    inline fun debug(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    inline fun info(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    inline fun warn(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    inline fun error(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    inline fun log(level: LogLevel, error: Throwable? = null, lazyMessage: () -> String) {
        if (isEnabled(level)) {
            val message = lazyMessage.invoke()
            log(level, message, error)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel): Boolean = backend.isEnabled(level, context)

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?) {
        val decoratedMessage = context.decorate(message, factory)
        backend.log(level, decoratedMessage, error, context, callerInfo)
    }

    /**
     * Returns a [Logger] to use logging context of the calling coroutine.
     *
     * @see Logger.withoutContext
     */
    fun withCoroutineContext(): Logger {
        return logger
    }

    /**
     * Returns a logger for the same class that use specified [context].
     *
     * Note that this instance's context **will not** be merged with the [context].
     */
    fun withContext(context: LoggingContext): ContextLogger {
        return logger.withContext(context)
    }

    /**
     * Returns a logger for the same class that use a context
     * composed of this instance's context and an element with specified [key] and [value].
     *
     * @see LoggingContext.add
     */
    fun addContext(key: String, value: Any): ContextLogger {
        return withContext(context.add(key, value))
    }

    override fun toString(): String {
        return "ContextLogger(context: $context, backend: $backend)"
    }

    companion object {

        private val callerInfo = CallerInfo(ContextLogger::class.java.name)

    }

}
