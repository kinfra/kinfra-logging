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
public class LoggingRequest(
    public val level: LogLevel,
    public val message: String,
    public val additionalData: LoggingAdditionalData,
    public val context: LoggingContext,
    public val decor: MessageDecor,
    public val caller: CallerInfo
) {

    /**
     * The [message] decorated with [decor].
     */
    public val decoratedMessage: String
        get() = decor.decorate(message)

    override fun toString(): String {
        return "LoggingRequest(level=$level, message='$message', data=$additionalData, context=$context)"
    }

}
