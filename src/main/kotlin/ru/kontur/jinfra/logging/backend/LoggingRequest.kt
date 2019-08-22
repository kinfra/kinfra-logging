package ru.kontur.jinfra.logging.backend

import ru.kontur.jinfra.logging.*
import ru.kontur.jinfra.logging.decor.MessageDecor

/**
 * Represents [Logger]'s request to its [backend][LoggerBackend] to log a message.
 *
 * @property level level of the message
 * @property message the message, as supplied by user
 * @property error a [Throwable] that should be logged together with the message
 * @property context context of the message
 * @property decor a decor containing context data
 * @property caller information about caller for location aware logging
 */
class LoggingRequest(
    val level: LogLevel,
    val message: String,
    val error: Throwable?,
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
        return "LoggingRequest(level=$level, message='$message', error=$error, context=$context, caller=$caller)"
    }

}
