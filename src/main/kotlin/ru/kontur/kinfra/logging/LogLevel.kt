package ru.kontur.kinfra.logging

/**
 * Importance level of a log message.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ;

    // for user extensions
    companion object

}
