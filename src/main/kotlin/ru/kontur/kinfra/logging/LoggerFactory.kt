package ru.kontur.kinfra.logging

import ru.kontur.kinfra.logging.backend.LoggerBackend
import ru.kontur.kinfra.logging.decor.MessageDecor
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
public abstract class LoggerFactory {

    /**
     * Obtains a [Logger] with a given [name].
     *
     * The name is usually a fully qualified name of some class.
     *
     * This method is thread safe. Calls [getLoggerBackend] internally.
     */
    public open fun getLogger(name: String): Logger {
        val backend = getLoggerBackend(name)
        return Logger(backend, this)
    }

    /**
     * Obtains [Logger] instance to use in specified [class][kClass].
     *
     * This method is thread safe. Calls [getLoggerBackend] internally.
     */
    public open fun getLogger(kClass: KClass<*>): Logger {
        val name = requireNotNull(kClass.qualifiedName) {
            "Class $kClass does not have a fully qualified name"
        }
        return getLogger(name)
    }

    /**
     * Provides [LoggerBackend] for a logger with a given [name].
     *
     * This method must be thread-safe.
     */
    protected abstract fun getLoggerBackend(name: String): LoggerBackend

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
    public abstract class Wrapper : LoggerFactory() {

        protected abstract val delegate: LoggerFactory

        override fun getLoggerBackend(name: String): LoggerBackend = delegate.getLoggerBackend(name)

        override fun getEmptyDecor(): MessageDecor = delegate.getEmptyDecor()

    }

    // for user extensions
    public companion object

}

/**
 * Obtains [Logger] instance to use in the current class (that is the class calling this method).
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun LoggerFactory.currentClassLogger(): Logger {
    /*
     * All internal callers of this function must be inline!
     * It is required for MethodHandles.lookup() to work correctly.
     */
    return getLogger(MethodHandles.lookup()!!)
}

@PublishedApi
internal fun LoggerFactory.getLogger(lookup: MethodHandles.Lookup): Logger {
    val clazz = getTopLevelClass(lookup.lookupClass())
    return getLogger(clazz.kotlin)
}

private tailrec fun getTopLevelClass(clazz: Class<*>): Class<*> {
    val enclosing = clazz.enclosingClass
    return if (enclosing == null) {
        clazz
    } else {
        getTopLevelClass(enclosing)
    }
}
