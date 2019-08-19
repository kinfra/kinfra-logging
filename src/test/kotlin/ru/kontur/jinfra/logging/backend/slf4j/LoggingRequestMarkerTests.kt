package ru.kontur.jinfra.logging.backend.slf4j

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.Marker
import ru.kontur.jinfra.logging.LogLevel
import ru.kontur.jinfra.logging.LoggingContext
import ru.kontur.jinfra.logging.backend.CallerInfo
import ru.kontur.jinfra.logging.backend.LoggingRequest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LoggingRequestMarkerTests {

    @Test
    fun serialized_successfully() {
        val request = LoggingRequest(LogLevel.INFO, "message", "message", null, LoggingContext.EMPTY, CallerInfo("foo"))
        val marker = LoggingRequestMarker(request)

        val data = ByteArrayOutputStream().let { baos ->
            ObjectOutputStream(baos).apply {
                writeObject(marker)
                flush()
            }
            baos.toByteArray()
        }

        val deserialized = ByteArrayInputStream(data).let { bais ->
            ObjectInputStream(bais).readObject()
        }

        check(deserialized is Marker)
        assertEquals(LoggingRequestMarker.NAME, deserialized.name)
    }

}
