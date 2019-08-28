package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.slf4j.Slf4jBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import ru.kontur.jinfra.logging.decor.PrefixMessageDecor
import kotlin.reflect.KClass

/**
 * Default implementation of [LoggerFactory].
 *
 * For now, it uses SLF4J as logging backend.
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    override val delegate: LoggerFactory = Slf4jBackend.Factory

    override fun getEmptyDecor(): MessageDecor = PrefixMessageDecor.EMPTY

}

// todo: remove before release
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated(
    "use member method",
    level = DeprecationLevel.HIDDEN,
    replaceWith = ReplaceWith("Logger.forClass(kClass)", imports = ["ru.kontur.jinfra.logging.Logger"])
)
fun Logger.Companion.forClass(kClass: KClass<*>): Logger {
    return DefaultLoggerFactory.getLogger(kClass)
}
