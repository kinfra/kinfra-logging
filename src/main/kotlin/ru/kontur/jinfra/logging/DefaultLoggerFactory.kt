package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackendProvider
import ru.kontur.jinfra.logging.backend.Slf4jBackend
import kotlin.reflect.KClass

private object DefaultLoggerFactory : LoggerFactory {

    private val backendProvider: LoggerBackendProvider = Slf4jBackend

    override fun getLogger(kClass: KClass<*>): Logger {
        val backend = backendProvider.forJavaClass(kClass.java, Logger::class.java)
        return Logger.backedBy(backend)
    }

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
