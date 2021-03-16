package ru.kontur.kinfra.logging

import org.junit.jupiter.api.Test

private val topLevelLogger = Logger.currentClass()

private val namedLogger = Logger.forName("foo.bar.baz")

class DefaultLoggerFactoryTests {

    private val classLogger = Logger.currentClass()

    @Test
    fun current_class_logger() {
        classLogger.info { "Class logger message" }
    }

    @Test
    fun companion_current_class_logger() {
        companionLogger.info { "Companion logger message" }
    }

    @Test
    fun top_level_logger() {
        topLevelLogger.info { "Top level logger message" }
    }

    @Test
    fun nested_object_logger() {
        Foo.Bar.logger.info { "Nested object logger message" }
    }

    @Test
    fun named_logger() {
        namedLogger.info { "Named logger message" }
    }

    object Foo {
        object Bar {
            val logger = Logger.currentClass()
        }
    }

    companion object {

        private val companionLogger = Logger.currentClass()

    }

}
