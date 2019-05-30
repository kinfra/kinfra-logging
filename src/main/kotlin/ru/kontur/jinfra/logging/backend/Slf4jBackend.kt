package ru.kontur.jinfra.logging.backend

import org.slf4j.Logger
import org.slf4j.event.EventConstants
import org.slf4j.spi.LocationAwareLogger
import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggerFactory
import ru.kontur.jinfra.logging.LoggingContext
import kotlin.reflect.KClass

internal abstract class Slf4jBackend private constructor() : LoggerBackend {

    protected abstract val slf4jLogger: Logger

    override fun isEnabled(level: LogLevel, context: LoggingContext): Boolean {
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

            with(slf4jLogger) {
                when (level) {
                    LogLevel.TRACE -> trace(message, error)
                    LogLevel.DEBUG -> debug(message, error)
                    LogLevel.INFO -> info(message, error)
                    LogLevel.WARN -> warn(message, error)
                    LogLevel.ERROR -> error(message, error)
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

            val slf4jLevel = when (level) {
                LogLevel.TRACE -> EventConstants.TRACE_INT
                LogLevel.DEBUG -> EventConstants.DEBUG_INT
                LogLevel.INFO -> EventConstants.INFO_INT
                LogLevel.WARN -> EventConstants.WARN_INT
                LogLevel.ERROR -> EventConstants.ERROR_INT
            }

            slf4jLogger.log(null, caller.facadeClassName, slf4jLevel, message, null, error)
        }

    }

    object Factory : LoggerFactory() {

        override fun getLoggerBackend(kClass: KClass<*>): LoggerBackend {
            val slf4jLogger: Logger = org.slf4j.LoggerFactory.getLogger(kClass.java)

            return if (slf4jLogger is LocationAwareLogger) {
                LocationAware(slf4jLogger)
            } else {
                Basic(slf4jLogger)
            }
        }

    }

}
