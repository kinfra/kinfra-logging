package ru.kontur.kinfra.logging.backend

/**
 * Additional data of a log event that accompanies a text message.
 */
public class LoggingAdditionalData(
    public val throwable: Throwable? = null
) {

    override fun toString(): String = buildString {
        append("{")
        throwable?.let { append("throwable=$it") }
        append("}")
    }

    public companion object {

        public val NONE: LoggingAdditionalData = LoggingAdditionalData()

    }

}
