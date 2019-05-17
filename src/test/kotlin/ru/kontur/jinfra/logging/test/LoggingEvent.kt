package ru.kontur.jinfra.logging.test

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggingContext

data class LoggingEvent(
    val level: LogLevel,
    val message: String,
    val error: Throwable?,
    val context: LoggingContext
)
