package ru.kontur.jinfra.logging.backend

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.EventConstants
import org.slf4j.event.Level
import org.slf4j.spi.LocationAwareLogger
import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggingContext

internal abstract class Slf4jBackend private constructor() : LoggerBackend {

    protected abstract val slf4jLogger: Logger

    override fun isEnabled(level: LogLevel): Boolean {
        return with(slf4jLogger) {
            when (level) {
                LogLevel.TRACE -> isTraceEnabled
                LogLevel.DEBUG -> isDebugEnabled
                LogLevel.INFO -> isInfoEnabled
                LogLevel.WARN -> isWarnEnabled
                LogLevel.ERROR -> isErrorEnabled
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun createMarker(context: LoggingContext): Marker? {
        // todo: use markers?
        return null
    }

    private class Basic(
        override val slf4jLogger: Logger
    ) : Slf4jBackend() {

        override fun log(
            level: LogLevel,
            message: String,
            error: Throwable?,
            context: LoggingContext,
            caller: CallerInfo
        ) {

            val marker: Marker? = createMarker(context)
            val fullMessage = context.decorate(message)

            with(slf4jLogger) {
                when (level) {
                    LogLevel.TRACE -> trace(marker, fullMessage, error)
                    LogLevel.DEBUG -> debug(marker, fullMessage, error)
                    LogLevel.INFO -> info(marker, fullMessage, error)
                    LogLevel.WARN -> warn(marker, fullMessage, error)
                    LogLevel.ERROR -> error(marker, fullMessage, error)
                }
            }
        }

    }

    private class LocationAware(
        override val slf4jLogger: LocationAwareLogger
    ) : Slf4jBackend() {

        override fun log(
            level: LogLevel,
            message: String,
            error: Throwable?,
            context: LoggingContext,
            caller: CallerInfo
        ) {

            val marker = createMarker(context)
            val fullMessage = context.decorate(message)
            val slf4jLevel = when (level) {
                LogLevel.TRACE -> EventConstants.TRACE_INT
                LogLevel.DEBUG -> EventConstants.DEBUG_INT
                LogLevel.INFO -> EventConstants.INFO_INT
                LogLevel.WARN -> EventConstants.WARN_INT
                LogLevel.ERROR -> EventConstants.ERROR_INT
            }

            slf4jLogger.log(marker, caller.facadeClassName, slf4jLevel, fullMessage, null, error)
        }

    }

    companion object : LoggerBackendProvider {

        override fun forJavaClass(jClass: Class<*>): LoggerBackend {
            val slf4jLogger: Logger = org.slf4j.LoggerFactory.getLogger(jClass)

            return if (slf4jLogger is LocationAwareLogger) {
                LocationAware(slf4jLogger)
            } else {
                Basic(slf4jLogger)
            }
        }

    }

}
