package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend

/**
 * @see Logger.Companion.currentClass
 * @see Logger.Companion.forClass
 */
class Logger {
    // todo: class documentation

    val context: LoggingContext

    private val backend: LoggerBackend

    /**
     * At most one Logger with empty context needs to be constructed with a given LoggerBackend.
     * This field refers that Logger. May contain `this`.
     */
    private val emptyContextLogger: Logger

    /**
     * At most one CoroutineLogger needs to be constructed with a given LoggerBackend.
     * Lazily initialized in the logger with empty context.
     */
    @Volatile
    private var coroutineLogger: CoroutineLogger? = null

    /**
     * Create a new logger with empty context.
     */
    private constructor(backend: LoggerBackend) {
        this.context = LoggingContext.EMPTY
        this.backend = backend
        this.emptyContextLogger = this
    }

    /**
     * Create a logger with specified non-empty [context].
     */
    private constructor(emptyContextLogger: Logger, context: LoggingContext) {
        this.context = context
        this.backend = emptyContextLogger.backend
        this.emptyContextLogger = emptyContextLogger
    }

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
     * Returns a [CoroutineLogger] to use logging context of the calling coroutine.
     *
     * @see CoroutineLogger.withoutContext
     */
    fun withCoroutineContext(): CoroutineLogger {
        return if (this != emptyContextLogger) {
            emptyContextLogger.withCoroutineContext()
        } else {
            coroutineLogger ?: CoroutineLogger(emptyContextLogger, backend).also {
                coroutineLogger = it
            }
        }
    }

    /**
     * Returns a [Logger] for the same class that use specified [context].
     *
     * Note that this instance's context **will not** be merged with the [context].
     */
    fun withContext(context: LoggingContext): Logger {
        return if (context == LoggingContext.EMPTY) {
            emptyContextLogger
        } else {
            Logger(emptyContextLogger, context)
        }
    }

    /**
     * Returns a [Logger] for the same class that use a context
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

        internal fun backedBy(backend: LoggerBackend): Logger = Logger(backend)

    }

}
