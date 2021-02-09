package ru.kontur.kinfra.logging.backend

/**
 * Additional data of a log event that accompanies a text message.
 */
class LoggingAdditionalData(
    val throwable: Throwable? = null
) {

    override fun toString() = buildString {
        append("{")
        throwable?.let { append("throwable=$it") }
        append("}")
    }

    companion object {

        val NONE = LoggingAdditionalData()

    }

}
