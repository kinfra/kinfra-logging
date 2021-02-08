package ru.kontur.kinfra.logging

import ru.kontur.kinfra.logging.builtin.BuiltinLoggerFactory

/**
 * Default [logger factory][LoggerFactory].
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    /**
     * Actual factory in use.
     */
    public override val delegate: LoggerFactory = BuiltinLoggerFactory()

}
