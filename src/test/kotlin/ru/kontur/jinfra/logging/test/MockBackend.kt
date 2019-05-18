package ru.kontur.jinfra.logging.test

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.LoggingContext

class MockBackend : LoggerBackend {

    private val recordedEvents: MutableList<LoggingEvent> = mutableListOf()

    var level: LogLevel = LogLevel.TRACE

    val events: List<LoggingEvent>
        get() = this.recordedEvents

    override fun isEnabled(level: LogLevel): Boolean {
        return level >= this.level
    }

    override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext) {
        val event = LoggingEvent(level, message, error, context)
        recordedEvents += event
    }
}
