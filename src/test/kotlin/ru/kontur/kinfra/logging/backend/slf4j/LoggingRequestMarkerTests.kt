package ru.kontur.kinfra.logging.backend.slf4j

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.Marker
import ru.kontur.kinfra.logging.LogLevel
import ru.kontur.kinfra.logging.LoggingContext
import ru.kontur.kinfra.logging.backend.CallerInfo
import ru.kontur.kinfra.logging.backend.LoggingAdditionalData
import ru.kontur.kinfra.logging.backend.LoggingRequest
import ru.kontur.kinfra.logging.decor.MessageDecor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LoggingRequestMarkerTests {

    @Test
    fun serialized_successfully() {
        val request = LoggingRequest(
            level = LogLevel.INFO,
            message = "message",
            additionalData = LoggingAdditionalData.NONE,
            context = LoggingContext.EMPTY,
            decor = MessageDecor.Nop,
            caller = CallerInfo("foo")
        )

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
