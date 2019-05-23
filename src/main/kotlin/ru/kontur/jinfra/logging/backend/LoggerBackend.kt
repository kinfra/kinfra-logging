package ru.kontur.jinfra.logging.backend

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggingContext

interface LoggerBackend {

    fun isEnabled(level: LogLevel): Boolean

    fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext, caller: CallerInfo)

    object Nop : LoggerBackend {

        override fun isEnabled(level: LogLevel): Boolean = false

        override fun log(
            level: LogLevel,
            message: String,
            error: Throwable?,
            context: LoggingContext,
            caller: CallerInfo
        ) = Unit

    }

    // for user extensions
    companion object

}

class CallerInfo internal constructor(
    val facadeClassName: String
)
