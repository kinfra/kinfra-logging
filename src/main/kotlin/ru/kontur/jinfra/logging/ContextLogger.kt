package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.backend.LoggingAdditionalData
import ru.kontur.jinfra.logging.backend.LoggingRequest

/**
 * A logger with a fixed [context] attached to it.
 *
 * Use-cases for ContextLogger are:
 *
 *  * You don't care about context at all **and** need logger methods to be non-`suspend`.
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

    /** [Log][log] a message with [DEBUG][LogLevel.DEBUG] level. */
    inline fun debug(error: Throwable? = null, lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    /** [Log][log] a message with [INFO][LogLevel.INFO] level. */
    inline fun info(error: Throwable? = null, lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    /** [Log][log] a message with [WARN][LogLevel.WARN] level. */
    inline fun warn(error: Throwable? = null, lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    /** [Log][log] a message with [ERROR][LogLevel.ERROR] level. */
    inline fun error(error: Throwable? = null, lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    /**
     * Log a message produced by [lazyMessage] lambda with specified [level].
     *
     * The lambda will be called only if logging [is enabled][LoggerBackend.isEnabled].
     *
     * @param error a [Throwable] that should be logged with the message
     */
    inline fun log(level: LogLevel, error: Throwable? = null, lazyMessage: MessageBuilder.() -> String) {
        if (isEnabled(level)) {
            val messageBuilder = MessageBuilder.STUB
            val message = lazyMessage.invoke(messageBuilder)
            log(level, message, messageBuilder, error)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel): Boolean = backend.isEnabled(level, context)

    @PublishedApi
    internal fun log(
        level: LogLevel,
        message: String,
        @Suppress("UNUSED_PARAMETER") messageBuilder: MessageBuilder,
        error: Throwable?
    ) {

        val additionalData = if (error == null) {
            LoggingAdditionalData.NONE
        } else {
            LoggingAdditionalData(
                throwable = error
            )
        }
        val request = LoggingRequest(
            level = level,
            message = message,
            additionalData = additionalData,
            context = this.context,
            decor = context.getDecor(factory),
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

    // todo: remove this eventually
    @PublishedApi
    @Deprecated("For ABI compatibility with versions <= 0.13.1", level = DeprecationLevel.HIDDEN)
    internal fun log(level: LogLevel, message: String, error: Throwable?) {
        log(level, message, MessageBuilder.STUB, error)
    }

    companion object {

        private val callerInfo = CallerInfo(ContextLogger::class.java.name)

    }

}
