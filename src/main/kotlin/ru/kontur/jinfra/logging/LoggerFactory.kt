package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass

abstract class LoggerFactory {

    /**
     * Obtains [Logger] instance to use in specified [class][kClass].
     */
    fun getLogger(kClass: KClass<*>): Logger {
        val backend = getLoggerBackend(kClass)
        return Logger(backend, this)
    }

    /**
     * Provides [LoggerBackend] for a logger to use in specified [class][kClass].
     *
     * This method must be thread-safe.
     */
    protected abstract fun getLoggerBackend(kClass: KClass<*>): LoggerBackend

    /**
     * Provides an instance of initial (empty) [MessageDecor].
     *
     * Default implementation returns [MessageDecor.Nop].
     */
    protected open fun getEmptyDecor(): MessageDecor = MessageDecor.Nop

    internal fun getEmptyDecorInternal(): MessageDecor = getEmptyDecor()

    /**
     * Delegates all calls to another LoggerFactory ([delegate]).
     *
     * Custom wrapping factories should extend this class to properly implement
     * new methods that may be added in the future.
     */
    abstract class Wrapper : LoggerFactory() {

        protected abstract val delegate: LoggerFactory

        override fun getLoggerBackend(kClass: KClass<*>) = delegate.getLoggerBackend(kClass)

        override fun getEmptyDecor() = delegate.getEmptyDecor()

    }

    // for user extensions
    companion object

}

/**
 * Obtains [Logger] instance to use in the current class (that is the class calling this method).
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
