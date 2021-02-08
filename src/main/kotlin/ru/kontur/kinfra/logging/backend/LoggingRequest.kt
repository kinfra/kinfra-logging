package ru.kontur.kinfra.logging.backend

import ru.kontur.kinfra.logging.LogLevel
import ru.kontur.kinfra.logging.Logger
import ru.kontur.kinfra.logging.LoggingContext
import ru.kontur.kinfra.logging.decor.MessageDecor

/**
 * Represents [Logger]'s request to its [backend][LoggerBackend] to log a message.
 *
 * @property level level of the message
 * @property message text of the message, as supplied by user
 * @property additionalData additional data that should be logged together with the text message
 * @property context context of the message
 * @property decor a decor containing context data
 * @property caller information about caller for location aware logging
 */
class LoggingRequest(
    val level: LogLevel,
    val message: String,
    val additionalData: LoggingAdditionalData,
    val context: LoggingContext,
    val decor: MessageDecor,
    val caller: CallerInfo
) {

    /**
     * The [message] decorated with [decor].
     */
    val decoratedMessage: String
        get() = decor.decorate(message)

    override fun toString(): String {
        return "LoggingRequest(level=$level, message='$message', data=$additionalData, context=$context)"
    }

}
