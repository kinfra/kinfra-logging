package ru.kontur.jinfra.logging

interface LoggerBackend {

    fun isEnabled(level: LogLevel): Boolean

    fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext)

    object Nop : LoggerBackend {
        override fun isEnabled(level: LogLevel): Boolean = false
        override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext) = Unit
    }

}
