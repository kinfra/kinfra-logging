package ru.kontur.jinfra.logging

interface LoggerBackendProvider {

    fun forJavaClass(jClass: Class<*>, facadeClass: Class<*>): LoggerBackend

}
