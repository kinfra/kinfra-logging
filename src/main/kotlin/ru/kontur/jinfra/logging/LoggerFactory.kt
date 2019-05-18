package ru.kontur.jinfra.logging

import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass

interface LoggerFactory {

    fun getLogger(kClass: KClass<*>): Logger

    // for user extensions
    companion object

}

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
