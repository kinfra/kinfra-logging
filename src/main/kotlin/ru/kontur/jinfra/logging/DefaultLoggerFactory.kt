package ru.kontur.jinfra.logging

import ru.kontur.jinfra.logging.backend.slf4j.Slf4jBackend
import ru.kontur.jinfra.logging.decor.MessageDecor
import ru.kontur.jinfra.logging.decor.PrefixMessageDecor

/**
 * Default implementation of [LoggerFactory].
 *
 * For now, it uses SLF4J as logging backend.
 */
object DefaultLoggerFactory : LoggerFactory.Wrapper() {

    override val delegate: LoggerFactory = Slf4jBackend.Factory

    override fun getEmptyDecor(): MessageDecor = PrefixMessageDecor.EMPTY

}
