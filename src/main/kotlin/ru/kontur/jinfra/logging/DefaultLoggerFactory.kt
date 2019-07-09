package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.Slf4jBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import ru.kontur.jinfra.logging.decor.PrefixMessageDecor
import kotlin.reflect.KClass

/**
 * Default implementation of [LoggerFactory].
 *
 * For now it uses SLF4J as logging backend.
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    override val delegate: LoggerFactory = Slf4jBackend.Factory

    override fun getEmptyDecor(): MessageDecor = PrefixMessageDecor.EMPTY

}

// todo: remove before release
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated(
    "use member method",
    replaceWith = ReplaceWith("Logger.forClass(kClass)", imports = ["ru.kontur.jinfra.logging.Logger"])
)
fun Logger.Companion.forClass(kClass: KClass<*>): Logger {
    return DefaultLoggerFactory.getLogger(kClass)
}

// todo: remove before release
@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated(
    "use member method",
    replaceWith = ReplaceWith("Logger.currentClass()", imports = ["ru.kontur.jinfra.logging.Logger"])
)
inline fun Logger.Companion.currentClass(): Logger {
    return DefaultLoggerFactory.currentClassLogger()
}
