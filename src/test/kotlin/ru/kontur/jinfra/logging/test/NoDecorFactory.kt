package ru.kontur.jinfra.logging.test

import ru.kontur.jinfra.logging.DefaultLoggerFactory
import ru.kontur.jinfra.logging.LoggerFactory
import ru.kontur.jinfra.logging.decor.MessageDecor

object NoDecorFactory : LoggerFactory.Wrapper() {

    override val delegate: LoggerFactory
        get() = DefaultLoggerFactory

    override fun getEmptyDecor() = MessageDecor.Nop

}
