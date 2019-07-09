package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.backend.LoggingRequest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

/**
 * An object used to log messages for a specified class.
 *
 * Loggers can be obtained from a [LoggerFactory].
 * There are convenient companion object methods to obtain a logger from the [default factory][DefaultLoggerFactory]:
 *
 *  * [Logger.currentClass] obtains a logger for use in current class,
 *    i.e. in the class calling that method.
 *
 *  * [Logger.forClass] obtains a logger for use in specified class.
 *
 * All logging methods are `suspend`. It allows them to implicitly use [LoggingContext] of the calling coroutine.
 * If you don't need this **and** you need a logger for non-`suspend` code consider using a [ContextLogger]
 * with empty context. It can be obtained via [withoutContext].
 *
 * @see Logger.currentClass
 * @see Logger.forClass
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

    /** [Log][log] a message with [TRACE][LogLevel.TRACE] level. */
    suspend inline fun trace(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.TRACE, error, lazyMessage)
    }

    /** [Log][log] a message with [DEBUG][LogLevel.DEBUG] level. */
    suspend inline fun debug(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    /** [Log][log] a message with [INFO][LogLevel.INFO] level. */
    suspend inline fun info(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    /** [Log][log] a message with [WARN][LogLevel.WARN] level. */
    suspend inline fun warn(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    /** [Log][log] a message with [ERROR][LogLevel.ERROR] level. */
    suspend inline fun error(error: Throwable? = null, lazyMessage: () -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    /**
     * Log a message with specified [level] produced by [lazyMessage] lambda.
     *
     * The lambda will be called only if logging [is enabled][LoggerBackend.isEnabled].
     *
     * @param error a [Throwable] that should be logged with the message
     */
    suspend inline fun log(level: LogLevel, error: Throwable? = null, lazyMessage: () -> String) {
        val context = coroutineContext
        if (isEnabled(level, context)) {
            val message = lazyMessage.invoke()
            log(level, message, error, context)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel, context: CoroutineContext): Boolean {
        return backend.isEnabled(level, context)
    }

    @PublishedApi
    internal fun log(level: LogLevel, message: String, error: Throwable?, context: CoroutineContext) {
        val loggingContext = LoggingContext.fromCoroutineContext(context)
        val decoratedMessage = loggingContext.decorate(message, factory)
        val request = LoggingRequest(
            level = level,
            message = message,
            decoratedMessage = decoratedMessage,
            error = error,
            context = loggingContext,
            caller = callerInfo
        )

        backend.log(request)
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

        /**
         * Obtains a [Logger] instance to use in specified [class][kClass] using default [LoggerFactory].
         */
        fun forClass(kClass: KClass<*>): Logger {
            return DefaultLoggerFactory.getLogger(kClass)
        }

        /**
         * Obtains a [Logger] instance to use in the current class using default [LoggerFactory].
         *
         * Usage:
         * ```
         *   class MyClass {
         *       private val logger = Logger.currentClass()
         *       ...
         *   }
         * ```
         *
         * Also can be used in a companion object:
         * ```
         *   class MyClass {
         *       ...
         *
         *       companion object {
         *           private val logger = Logger.currentClass()
         *           ...
         *       }
         *   }
         * ```
         *
         * And in top-level property:
         * ```
         * private val logger = Logger.currentClass()
         * ```
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun currentClass(): Logger {
            return DefaultLoggerFactory.currentClassLogger()
        }

    }

}
