package ru.kontur.jinfra.logging.internal

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.spi.LocationAwareLogger
import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggerBackend
import ru.kontur.jinfra.logging.LoggerBackendProvider
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

    protected fun getFullMessage(message: String, context: LoggingContext): String {
        return context.prefix + message
    }

    private class Basic(
        override val slf4jLogger: Logger
    ) : Slf4jBackend() {

        override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext) {
            val marker: Marker? = createMarker(context)
            val fullMessage = getFullMessage(message, context)

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
        override val slf4jLogger: LocationAwareLogger,
        facadeClass: Class<*>
    ) : Slf4jBackend() {

        private val facadeFqcn = facadeClass.name

        override fun log(level: LogLevel, message: String, error: Throwable?, context: LoggingContext) {
            val marker = createMarker(context)
            val fullMessage = getFullMessage(message, context)
            val slf4jLevel = when (level) {
                LogLevel.TRACE -> Level.TRACE
                LogLevel.DEBUG -> Level.DEBUG
                LogLevel.INFO -> Level.INFO
                LogLevel.WARN -> Level.WARN
                LogLevel.ERROR -> Level.ERROR
            }

            slf4jLogger.log(marker, facadeFqcn, slf4jLevel.toInt(), fullMessage, null, error)
        }

    }

    companion object : LoggerBackendProvider {

        override fun forJavaClass(jClass: Class<*>, facadeClass: Class<*>): LoggerBackend {
            val slf4jLogger: Logger = org.slf4j.LoggerFactory.getLogger(jClass)

            return if (slf4jLogger is LocationAwareLogger) {
                LocationAware(slf4jLogger, facadeClass)
            } else {
                Basic(slf4jLogger)
            }
        }

    }

}
