package ru.kontur.kinfra.logging.test

import ru.kontur.kinfra.logging.LogLevel
import ru.kontur.kinfra.logging.LoggingContext

data class LoggingEvent(
    val level: LogLevel,
    val message: String,
    val error: Throwable?,
    val context: LoggingContext,
    val caller: StackTraceElement
)
