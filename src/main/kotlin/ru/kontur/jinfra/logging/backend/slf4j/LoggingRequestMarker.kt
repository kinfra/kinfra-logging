package ru.kontur.jinfra.logging.backend.slf4j

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import ru.kontur.jinfra.logging.backend.LoggingRequest

/**
 * Marker that allows an SLF4J implementation to access [LoggingRequest].
 *
 * Note that it does not survive serialization.
 */
class LoggingRequestMarker private constructor(
    val request: LoggingRequest,
    private val delegate: Marker
) : Marker by delegate {

    constructor(request: LoggingRequest) : this(request, MarkerFactory.getDetachedMarker(NAME))

    // Serialize plain marker without non-serializable LoggingRequest
    private fun writeReplace(): Any = delegate

    companion object {

        const val NAME = "LoggingRequest"

    }

}
