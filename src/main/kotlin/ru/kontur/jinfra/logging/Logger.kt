package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend

/**
 * @see Logger.Companion.currentClass
 * @see Logger.Companion.forClass
 */
class Logger internal constructor(
    private val backend: LoggerBackend,
    val context: LoggingContext
) {
    // todo: class documentation

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
    internal fun isEnabled(level: LogLevel): Boolean = backend.isEnabled(level)

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?) {
        backend.log(level, message, error, context, callerInfo)
    }

    /**
     * Returns a [CoroutineLogger] backed by the same [LoggerBackend].
     *
     * @see CoroutineLogger.withoutContext
     */
    fun withCoroutineContext(): CoroutineLogger {
        return CoroutineLogger(backend)
    }

    /**
     * Returns a [Logger] backed by the same [LoggerBackend] that use specified [context].
     *
     * Note that this instance's context **will not** be merged with the [context].
     */
    fun withContext(context: LoggingContext): Logger {
        return Logger(backend, context)
    }

    /**
     * Returns a [Logger] backed by the same [LoggerBackend] that use a context
     * composed of this instance's context and an element with specified [key] and [value].
     *
     * @see LoggingContext.add
     */
    fun addContext(key: String, value: Any): Logger {
        return withContext(context.add(key, value))
    }

    override fun toString(): String {
        return "Logger(context: $context, backend: $backend)"
    }

    companion object {

        private val callerInfo = CallerInfo(Logger::class.java.name)

        internal fun backedBy(backend: LoggerBackend): Logger = Logger(backend, LoggingContext.EMPTY)

    }

}
