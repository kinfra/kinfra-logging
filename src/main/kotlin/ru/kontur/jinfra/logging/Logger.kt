package ru.kontur.jinfra.logging

class Logger internal constructor(
    private val backend: LoggerBackend,
    val context: LoggingContext
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
    internal fun isEnabled(level: LogLevel): Boolean = backend.isEnabled(level)

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?) {
        backend.log(level, message, error, context)
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
     * @see LoggingContext.with
     */
    fun addContext(key: String, value: Any): Logger {
        return Logger(backend, context.plus(key, value.toString()))
    }

    override fun toString(): String {
        return "Logger(context: $context, backend: $backend)"
    }

    companion object {

        fun backedBy(backend: LoggerBackend): Logger = Logger(backend, LoggingContext.EMPTY)

    }

}
