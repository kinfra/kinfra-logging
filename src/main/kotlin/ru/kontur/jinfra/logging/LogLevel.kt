package ru.kontur.jinfra.logging

/**
 * Importance level of a log message.
 */
enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ;

    // for user extensions
    companion object

}
