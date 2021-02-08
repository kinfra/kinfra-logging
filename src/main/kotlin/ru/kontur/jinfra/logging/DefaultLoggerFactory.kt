package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.builtin.BuiltinLoggerFactory

/**
 * Default [logger factory][LoggerFactory].
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    /**
     * Actual factory in use.
     */
    public override val delegate: LoggerFactory = BuiltinLoggerFactory()

}
