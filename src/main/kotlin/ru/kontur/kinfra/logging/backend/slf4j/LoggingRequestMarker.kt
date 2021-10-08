package ru.kontur.kinfra.logging.backend.slf4j

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import ru.kontur.kinfra.logging.backend.LoggingRequest

/**
 * Marker that allows an SLF4J implementation to access [LoggingRequest].
 *
 * Note that it does not survive serialization.
 */
public class LoggingRequestMarker private constructor(
    public val request: LoggingRequest,
    private val delegate: Marker
) : Marker by delegate {

    public constructor(request: LoggingRequest) : this(request, MarkerFactory.getDetachedMarker(NAME))

    // Serialize a plain marker without non-serializable LoggingRequest
    private fun writeReplace(): Any = delegate

    public companion object {

        public const val NAME: String = "LoggingRequest"

    }

}
