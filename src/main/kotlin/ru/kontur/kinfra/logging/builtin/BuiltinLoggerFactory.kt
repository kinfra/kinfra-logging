package ru.kontur.kinfra.logging.builtin

import ru.kontur.kinfra.logging.LoggerFactory
import ru.kontur.kinfra.logging.backend.LoggerBackend
import ru.kontur.kinfra.logging.backend.slf4j.Slf4jBackend
import ru.kontur.kinfra.logging.decor.DefaultMessageDecor
import ru.kontur.kinfra.logging.decor.MessageDecor

/**
 * Default logger factory that built into the library.
 *
 * For now, it uses SLF4J to access underlying logging system
 * and [DefaultMessageDecor] as message decor.
 */
internal class BuiltinLoggerFactory : LoggerFactory() {

    override fun getLoggerBackend(name: String): LoggerBackend {
        return Slf4jBackend.forName(name)
    }

    override fun getEmptyDecor(): MessageDecor {
        return DefaultMessageDecor.EMPTY
    }

}
