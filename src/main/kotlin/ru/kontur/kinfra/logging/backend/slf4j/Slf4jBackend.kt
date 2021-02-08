package ru.kontur.kinfra.logging.backend.slf4j

import org.slf4j.Logger
import org.slf4j.MDC
import org.slf4j.event.EventConstants
import org.slf4j.spi.LocationAwareLogger
import ru.kontur.kinfra.logging.LogLevel
import ru.kontur.kinfra.logging.LoggingContext
import ru.kontur.kinfra.logging.backend.LoggerBackend
import ru.kontur.kinfra.logging.backend.LoggingRequest
import kotlin.reflect.KClass

internal abstract class Slf4jBackend private constructor() : LoggerBackend {

    protected abstract val slf4jLogger: Logger

    override fun isEnabled(level: LogLevel, context: LoggingContext): Boolean {
        return with(slf4jLogger) {
            when (level) {
                LogLevel.DEBUG -> isDebugEnabled
                LogLevel.INFO -> isInfoEnabled
                LogLevel.WARN -> isWarnEnabled
                LogLevel.ERROR -> isErrorEnabled
            }
        }
    }

    protected inline fun withMdc(context: LoggingContext, block: () -> Unit) {
        try {
            populateMdc(context)
            block()
        } finally {
            cleanupMdc(context)
        }
    }

    private fun populateMdc(context: LoggingContext) {
        if (!context.isEmpty()) {
            for (element in context.elements) {
                MDC.put(element.key, element.value)
            }
        }
    }

    private fun cleanupMdc(context: LoggingContext) {
        if (!context.isEmpty()) {
            for (element in context.elements) {
                MDC.remove(element.key)
            }
        }
    }

    override fun toString(): String {
        return "SLF4J(${slf4jLogger.name})"
    }

    private class Basic(
        override val slf4jLogger: Logger
    ) : Slf4jBackend() {

        override fun log(request: LoggingRequest) {
            val marker = LoggingRequestMarker(request)
            val message = request.decoratedMessage
            val error = request.additionalData.throwable

            withMdc(request.context) {
                with(slf4jLogger) {
                    when (request.level) {
                        LogLevel.DEBUG -> debug(marker, message, error)
                        LogLevel.INFO -> info(marker, message, error)
                        LogLevel.WARN -> warn(marker, message, error)
                        LogLevel.ERROR -> error(marker, message, error)
                    }
                }
            }
        }

    }

    private class LocationAware(
        override val slf4jLogger: LocationAwareLogger
    ) : Slf4jBackend() {

        override fun log(request: LoggingRequest) {
            val marker = LoggingRequestMarker(request)
            val slf4jLevel = when (request.level) {
                LogLevel.DEBUG -> EventConstants.DEBUG_INT
                LogLevel.INFO -> EventConstants.INFO_INT
                LogLevel.WARN -> EventConstants.WARN_INT
                LogLevel.ERROR -> EventConstants.ERROR_INT
            }
            val message = request.decoratedMessage
            val error = request.additionalData.throwable
            val fqcn = request.caller.facadeClassName

            withMdc(request.context) {
                slf4jLogger.log(marker, fqcn, slf4jLevel, message, null, error)
            }
        }

    }

    companion object {

        fun forClass(kClass: KClass<*>): LoggerBackend {
            val slf4jLogger: Logger = org.slf4j.LoggerFactory.getLogger(kClass.java)

            return if (slf4jLogger is LocationAwareLogger) {
                LocationAware(slf4jLogger)
            } else {
                Basic(slf4jLogger)
            }
        }

    }

}
