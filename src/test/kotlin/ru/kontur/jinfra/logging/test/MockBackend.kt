package ru.kontur.jinfra.logging.test

import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.LoggingContext
import ru.kontur.jinfra.logging.backend.CallerInfo
import kotlin.coroutines.CoroutineContext

class MockBackend : LoggerBackend {

    private val recordedEvents: MutableList<LoggingEvent> = mutableListOf()

    var level: LogLevel? = LogLevel.TRACE

    val events: List<LoggingEvent>
        get() = this.recordedEvents

    override fun isEnabled(level: LogLevel, context: CoroutineContext): Boolean {
        val currentLevel = this.level
        val loggingContext = LoggingContext.fromCoroutineContext(context)

        return currentLevel != null && currentLevel <= level && loggingContext["ignore"] != "true"
    }

    override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext, caller: CallerInfo) {
        val actualCallerFrame = findActualCaller(caller)
        val event = LoggingEvent(level, message, error, context, actualCallerFrame)
        recordedEvents += event
    }

    private fun findActualCaller(callerInfo: CallerInfo): StackTraceElement {
        val throwable = Throwable()
        val stackTrace = throwable.stackTrace
        val facadeClassName = callerInfo.facadeClassName
        val facadeIndex = stackTrace.indexOfFirst { it.className == facadeClassName }

        if (facadeIndex == -1) {
            throw IllegalArgumentException("Failed to find facade in stack trace: $facadeClassName")
        } else {
            return stackTrace.drop(facadeIndex).dropWhile { it.className == facadeClassName }.first()
        }
    }

}
