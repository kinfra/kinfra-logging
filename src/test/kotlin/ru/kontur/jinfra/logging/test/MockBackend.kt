package ru.kontur.jinfra.logging.test

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.LoggingContext
import ru.kontur.jinfra.logging.backend.CallerInfo

class MockBackend : LoggerBackend {

    private val recordedEvents: MutableList<LoggingEvent> = mutableListOf()

    var level: LogLevel? = LogLevel.TRACE

    val events: List<LoggingEvent>
        get() = this.recordedEvents

    override fun isEnabled(level: LogLevel, context: LoggingContext): Boolean {
        val currentLevel = this.level
        return currentLevel != null && currentLevel <= level && context["ignore"] != "true"
    }

    override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext, caller: CallerInfo) {
        val actualCallerFrame = findActualCaller(caller)
        val event = LoggingEvent(level, message, error, context, actualCallerFrame)
        recordedEvents += event
    }

    private fun findActualCaller(callerInfo: CallerInfo): StackTraceElement {
        val throwable = Throwable()
        val stackTrace = throwable.stackTrace
        val facadeIndex = stackTrace.indexOfFirst { it.className == callerInfo.facadeClassName }

        if (facadeIndex == -1) {
            throw IllegalArgumentException("Failed to find facade in stack trace: ${callerInfo.facadeClassName}")
        } else {
            return stackTrace[facadeIndex + 1]
        }
    }

}
