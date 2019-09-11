package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.builtin.BuiltinLoggerFactory
import kotlin.reflect.KClass

/**
 * Default [logger factory][LoggerFactory].
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    /**
     * Actual factory in use.
     */
    override val delegate: LoggerFactory = BuiltinLoggerFactory()

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
