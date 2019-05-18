package ru.kontur.jinfra.logging

import kotlin.reflect.KClass

interface LoggerFactory {

    fun getLogger(kClass: KClass<*>): Logger

}
