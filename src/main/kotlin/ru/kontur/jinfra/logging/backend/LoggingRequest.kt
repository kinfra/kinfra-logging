package ru.kontur.jinfra.logging.backend

import ru.kontur.jinfra.logging.*

/**
 * Represents [Logger]'s request to its [backend][LoggerBackend] to log a message.
 *
 * @param level Level of the message
 * @param message Decorated message
 * @param error [Throwable] that should be logged together with the message
 * @param context Context of the message
 * @param caller Information about caller for location aware logging
 */
class LoggingRequest(
    val level: LogLevel,
    val message: String,
    val error: Throwable?,
    val context: LoggingContext,
    val caller: CallerInfo
) {

    override fun toString(): String {
        return "LoggingRequest(level=$level, message='$message', error=$error, context=$context, caller=$caller)"
    }

}
