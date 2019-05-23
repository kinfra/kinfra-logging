package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass

abstract class LoggerFactory {

    /**
     * Obtains [Logger] instance to use in specified [class][kClass].
     */
    fun getLogger(kClass: KClass<*>): Logger {
        val backend = getLoggerBackend(kClass.java)
        return Logger.backedBy(backend)
    }

    /**
     * Provides [LoggerBackend] for a logger to use in specified [class][jClass].
     */
    abstract fun getLoggerBackend(jClass: Class<*>): LoggerBackend

    // for user extensions
    companion object

}

/**
 * Obtains [Logger] instance to use in the current class using default [LoggerFactory].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun LoggerFactory.currentClassLogger(): Logger {
    val currentClass = getCallingClass()
    return getLogger(currentClass.kotlin)
}

/**
 * Obtains caller's class via [MethodHandles].
 *
 * **Note:** All internal callers of this function must be inline!
 * It is required for [MethodHandles.lookup] to work correctly.
 */
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun getCallingClass(): Class<*> {
    val currentClass = MethodHandles.lookup().lookupClass()
    // skip companion object's class
    return currentClass.enclosingClass ?: currentClass
}
