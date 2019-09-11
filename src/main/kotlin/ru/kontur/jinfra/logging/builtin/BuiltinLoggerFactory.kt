package ru.kontur.jinfra.logging.builtin

import ru.kontur.jinfra.logging.LoggerFactory
import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.backend.slf4j.Slf4jBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import ru.kontur.jinfra.logging.decor.PrefixMessageDecor
import kotlin.reflect.KClass

/**
 * Default logger factory that built into the library.
 *
 * For now, it uses SLF4J to access underlying logging system
 * and [PrefixMessageDecor] as message decor.
 */
internal class BuiltinLoggerFactory : LoggerFactory() {

    override fun getLoggerBackend(kClass: KClass<*>): LoggerBackend {
        return Slf4jBackend.forClass(kClass)
    }

    override fun getEmptyDecor(): MessageDecor {
        return PrefixMessageDecor.EMPTY
    }

}
