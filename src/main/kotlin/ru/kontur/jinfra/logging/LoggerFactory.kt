package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.LoggerBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass

/**
 * Entry point to an implementation of logging.
 *
 * There are two sides of this class:
 *
 *  * Front-end
 *
 *    From the user perspective, the most important method is [getLogger].
 *    It is called directly by user code to obtain a [Logger] to use.
 *
 *    This method can be overridden, but most implementations don't need that.
 *
 *  * Back-end
 *
 *    This side is represented by the methods to be implemented by concrete LoggerFactory:
 *
 *     * `getLoggerBackend()`: provides a [LoggerBackend] to use by logger returned from [getLogger].
 *
 *     * `getEmptyDecor()`: provides a [MessageDecor] to render context data in log messages.
 */
abstract class LoggerFactory {

    /**
     * Obtains [Logger] instance to use in specified [class][kClass].
     *
     * This method is thread safe. Calls [getLoggerBackend] internally.
     */
    open fun getLogger(kClass: KClass<*>): Logger {
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
    /*
     * All internal callers of this function must be inline!
     * It is required for [MethodHandles.lookup] to work correctly.
     */
    return getLogger(MethodHandles.lookup()!!)
}

@PublishedApi
internal fun LoggerFactory.getLogger(lookup: MethodHandles.Lookup): Logger {
    val clazz = lookup.lookupClass().let {
        // skip companion object's class
        it.enclosingClass ?: it
    }

    return getLogger(clazz.kotlin)
}
