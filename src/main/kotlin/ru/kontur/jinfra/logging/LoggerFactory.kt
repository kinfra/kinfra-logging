package ru.kontur.jinfra.logging

import kotlin.reflect.KClass

interface LoggerFactory {

    fun getLogger(kClass: KClass<*>): Logger

    fun getContextLogger(kClass: KClass<*>): ContextLogger

}
