package ru.kontur.jinfra.logging.backend

interface LoggerBackendProvider {

    fun forJavaClass(jClass: Class<*>, facadeClass: Class<*>): LoggerBackend

}
