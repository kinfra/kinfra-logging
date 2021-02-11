package ru.kontur.kinfra.logging.backend

import ru.kontur.kinfra.logging.LogLevel
import ru.kontur.kinfra.logging.Logger
import ru.kontur.kinfra.logging.LoggerFactory
import ru.kontur.kinfra.logging.LoggingContext

/**
 * Called by [Logger] to perform logging.
 * The [Logger] itself is a user-facing class that is neither extensible nor composable.
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
     * Implementation may use [LoggingContext.current] to make a decision.
     */
    fun isEnabled(level: LogLevel): Boolean

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
        override fun isEnabled(level: LogLevel) = false

        /**
         * Does nothing.
         */
        override fun log(request: LoggingRequest) = Unit

    }

    // for user extensions
    companion object

}
