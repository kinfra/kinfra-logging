package ru.kontur.kinfra.logging

import ru.kontur.kinfra.logging.backend.CallerInfo
import ru.kontur.kinfra.logging.backend.LoggerBackend
import ru.kontur.kinfra.logging.backend.LoggingAdditionalData
import ru.kontur.kinfra.logging.backend.LoggingRequest
import kotlin.reflect.KClass

/**
 * An object used to log messages for a particular class.
 *
 * Loggers can be obtained from a [LoggerFactory].
 * There are convenient companion object methods to obtain a logger from the [default factory][DefaultLoggerFactory]:
 *
 *  * [Logger.currentClass] obtains a logger for use in current class,
 *    i.e. in the class calling that method.
 *
 *  * [Logger.forClass] obtains a logger for use in specified class.
 *
 * @see Logger.currentClass
 * @see Logger.forClass
 */
class Logger internal constructor(
    private val backend: LoggerBackend,
    private val factory: LoggerFactory
) {

    /** [Log][log] a message with [DEBUG][LogLevel.DEBUG] level. */
    inline fun debug(error: Throwable? = null, crossinline lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.DEBUG, error, lazyMessage)
    }

    /** [Log][log] a message with [INFO][LogLevel.INFO] level. */
    inline fun info(error: Throwable? = null, crossinline lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.INFO, error, lazyMessage)
    }

    /** [Log][log] a message with [WARN][LogLevel.WARN] level. */
    inline fun warn(error: Throwable? = null, crossinline lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.WARN, error, lazyMessage)
    }

    /** [Log][log] a message with [ERROR][LogLevel.ERROR] level. */
    inline fun error(error: Throwable? = null, crossinline lazyMessage: MessageBuilder.() -> String) {
        log(LogLevel.ERROR, error, lazyMessage)
    }

    /**
     * Log a message produced by [lazyMessage] lambda with specified [level].
     *
     * The lambda will be called only if logging [is enabled][LoggerBackend.isEnabled].
     *
     * @param error a [Throwable] that should be logged with the message
     */
    // crossinline disallows non-local returns in the lambda
    inline fun log(level: LogLevel, error: Throwable? = null, crossinline lazyMessage: MessageBuilder.() -> String) {
        val context = LoggingContext.current()
        if (isEnabled(level, context)) {
            val messageBuilder = MessageBuilder.STUB
            val message = lazyMessage.invoke(messageBuilder)
            log(level, message, messageBuilder, error, context)
        }
    }

    @PublishedApi
    internal fun isEnabled(level: LogLevel, context: LoggingContext): Boolean {
        return backend.isEnabled(level, context)
    }

    @PublishedApi
    internal fun log(
        level: LogLevel,
        message: String,
        @Suppress("UNUSED_PARAMETER")
        messageBuilder: MessageBuilder,
        error: Throwable?,
        context: LoggingContext
    ) {

        val loggingContext = LoggingContext.fromCoroutineContext(context)
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
            context = loggingContext,
            decor = loggingContext.getDecor(factory.getEmptyDecorInternal()),
            caller = callerInfo
        )

        backend.log(request)
    }

    override fun toString(): String {
        return "Logger(backend: $backend)"
    }

    companion object {

        private val callerInfo = CallerInfo(facadeClassName = Logger::class.java.name)

        /**
         * Obtains a [Logger] instance to use in specified [class][kClass] using default [LoggerFactory].
         *
         * This method can be useful in an open class:
         * ```
         *     open class BaseClass {
         *         protected val logger = Logger.forClass(this::class)
         *         ...
         *     }
         * ```
         *
         * Note that [Logger.currentClass] would return a logger for `BaseClass`.
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
