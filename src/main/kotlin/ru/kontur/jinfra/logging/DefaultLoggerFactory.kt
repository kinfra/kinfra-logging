package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.context.ContextLogger
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

// inline is required for lookup() to work correctly
@Suppress("NOTHING_TO_INLINE")
inline fun Logger.Companion.currentClass(): Logger {
    val currentClass = MethodHandles.lookup().lookupClass()
    return DefaultLoggerFactory.getLogger(currentClass.kotlin)
}

// inline is required for lookup() to work correctly
@Suppress("NOTHING_TO_INLINE")
inline fun ContextLogger.Companion.currentClass(): ContextLogger {
    val currentClass = MethodHandles.lookup().lookupClass()
    return DefaultLoggerFactory.getContextLogger(currentClass.kotlin)
}
