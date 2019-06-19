package ru.kontur.jinfra.logging.backend

import ru.kontur.jinfra.logging.*
import kotlin.coroutines.CoroutineContext

/**
 * Called by [Logger] and [ContextLogger] to perform logging.
 * The [Logger] itself is a user-faced class that is not extensible nor composable.
 *
 * In order to use custom LoggerBackend one should create custom [LoggerFactory] and return
 * the backend from [LoggerFactory.getLoggerBackend] method.
 */
interface LoggerBackend {

    /**
     * Determines if a message with a given [level] should be logged.
     *
     * If the returned value is `false`, logger will not evaluate the message and call [log] method.
     *
     * Context of the message may be taken into consideration as well as the level.
     * If the implementation is going to do this, it should acquire [LoggingContext] from the [context]:
     * ```
     * val loggingContext = LoggingContext.fromCoroutineContext(context)
     * ```
     *
     * The [context] is **not** guaranteed to be any of:
     *
     *  * LoggingContext itself
     *  * A coroutine context containing LoggingContext
     *  * Actual context of the calling coroutine
     *
     * Thus it is **strongly discouraged** to use [context] any way apart from `LoggingContext.fromCoroutineContext()`.
     *
     */
    fun isEnabled(level: LogLevel, context: CoroutineContext): Boolean

    /**
     * Log a message.
     *
     * Implementation should not do additional filtering here, but do it in [isEnabled].
     */
    fun log(request: LoggingRequest)

    /**
     * Implementation that do nothing.
     */
    object Nop : LoggerBackend {

        /**
         * Returns `false`.
         */
        override fun isEnabled(level: LogLevel, context: CoroutineContext) = false

        /**
         * Does nothing.
         */
        override fun log(request: LoggingRequest) = Unit

    }

    // for user extensions
    companion object

}
