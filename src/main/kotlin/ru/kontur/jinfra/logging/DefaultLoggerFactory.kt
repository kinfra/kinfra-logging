package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.internal.Slf4jBackend
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass

object DefaultLoggerFactory : LoggerFactory {

    private val backendProvider: LoggerBackendProvider = Slf4jBackend

    override fun getLogger(kClass: KClass<*>): Logger {
        val backend = backendProvider.forJavaClass(kClass.java, Logger::class.java)
        return Logger.backedBy(backend)
    }

    override fun getContextLogger(kClass: KClass<*>): ContextLogger {
        val backend = backendProvider.forJavaClass(kClass.java, ContextLogger::class.java)
        return ContextLogger.backedBy(backend)
    }

}

fun Logger.Companion.forClass(kClass: KClass<*>): Logger {
    return DefaultLoggerFactory.getLogger(kClass)
}

fun ContextLogger.Companion.forClass(kClass: KClass<*>): ContextLogger {
    return DefaultLoggerFactory.getContextLogger(kClass)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Logger.Companion.currentClass(): Logger {
    val currentClass = getCallingClass()
    return DefaultLoggerFactory.getLogger(currentClass.kotlin)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ContextLogger.Companion.currentClass(): ContextLogger {
    val currentClass = getCallingClass()
    return DefaultLoggerFactory.getContextLogger(currentClass.kotlin)
}

// inline is required for lookup() to work correctly
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun getCallingClass(): Class<*> {
    val currentClass = MethodHandles.lookup().lookupClass()
    // skip companion
    return currentClass.enclosingClass ?: currentClass
}
