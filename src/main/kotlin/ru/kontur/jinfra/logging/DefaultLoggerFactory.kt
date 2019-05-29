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
object DefaultLoggerFactory : LoggerFactory.Wrapper(Slf4jBackend.Factory) {

    override fun getEmptyDecor(): MessageDecor = PrefixMessageDecor.EMPTY

}

/**
 * Obtains [Logger] instance to use in specified [class][kClass] using default [LoggerFactory].
 */
fun Logger.Companion.forClass(kClass: KClass<*>): Logger {
    return DefaultLoggerFactory.getLogger(kClass)
}

/**
 * Obtains [Logger] instance to use in the current class using default [LoggerFactory].
 *
 * Usage:
 * ```
 *   class MyClass {
 *       private val logger = Logger.currentClass()
 *       ...
 *   }
 * ```
 * Also can be used in a companion object:
 * ```
 *   class MyClass {
 *       ...
 *
 *       companion object {
 *           private val logger = Logger.currentClass()
 *           ...
 *       }
 *   }
 * ```
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Logger.Companion.currentClass(): Logger {
    return DefaultLoggerFactory.currentClassLogger()
}
