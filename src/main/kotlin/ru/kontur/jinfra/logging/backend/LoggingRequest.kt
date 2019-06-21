package ru.kontur.jinfra.logging.backend

import ru.kontur.jinfra.logging.*
import ru.kontur.jinfra.logging.decor.MessageDecor

/**
 * Represents [Logger]'s request to its [backend][LoggerBackend] to log a message.
 *
 * @param level level of the message
 * @param message the message, as supplied by user
 * @param decoratedMessage [message] decorated with [MessageDecor]
 * @param error [Throwable] that should be logged together with the message
 * @param context Context of the message
 * @param caller Information about caller for location aware logging
 */
class LoggingRequest(
    val level: LogLevel,
    val message: String,
    val decoratedMessage: String,
    val error: Throwable?,
    val context: LoggingContext,
    val caller: CallerInfo
) {

    override fun toString(): String {
        return "LoggingRequest(level=$level, message='$message', error=$error, context=$context, caller=$caller)"
    }

}
