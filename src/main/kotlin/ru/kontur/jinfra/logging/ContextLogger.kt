package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.backend.LoggingRequest

/**
 * A logger with a fixed [context] attached to it.
 *
 * Use-cases for ContextLogger are:
 *
 *  * You dont care about context at all **and** need logger methods to be non-`suspend`.
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

    /** [Log][log] a message with [TRACE][LogLevel.TRACE] level. */
    inline fun trace(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.TRACE, error, lazyMessage)
    }

    /** [Log][log] a message with [DEBUG][LogLevel.DEBUG] level. */
    inline fun debug(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    /** [Log][log] a message with [INFO][LogLevel.INFO] level. */
    inline fun info(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    /** [Log][log] a message with [WARN][LogLevel.WARN] level. */
    inline fun warn(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    /** [Log][log] a message with [ERROR][LogLevel.ERROR] level. */
    inline fun error(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    /**
     * Log a message with specified [level] produced by [lazyMessage] lambda.
     *
     * The lambda will be called only if logging [is enabled][LoggerBackend.isEnabled].
     *
     * @param error a [Throwable] that should be logged with the message
     */
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
        val request = LoggingRequest(
            level = level,
            message = message,
            decoratedMessage = decoratedMessage,
            error = error,
            context = this.context,
            caller = callerInfo
        )

        backend.log(request)
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
