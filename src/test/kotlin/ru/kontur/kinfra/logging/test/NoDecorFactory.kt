package ru.kontur.kinfra.logging.test

import ru.kontur.kinfra.logging.DefaultLoggerFactory
import ru.kontur.kinfra.logging.LoggerFactory
import ru.kontur.kinfra.logging.decor.MessageDecor

object NoDecorFactory : LoggerFactory.Wrapper() {

    override val delegate: LoggerFactory
        get() = DefaultLoggerFactory

    override fun getEmptyDecor() = MessageDecor.Nop

}
