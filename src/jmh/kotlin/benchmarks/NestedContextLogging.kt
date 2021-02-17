package benchmarks

import benchmarks.util.BenchmarkState
import benchmarks.util.runNonSuspending
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.openjdk.jmh.annotations.Benchmark
import org.slf4j.MDC
import ru.kontur.kinfra.logging.LoggingContext
import ru.kontur.kinfra.logging.withLoggingContext

open class NestedContextLogging {

    @Benchmark
    open fun enabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        withLoggingContext("key1", "value1") {
            logger.info { "Info message 1" }

            withLoggingContext("key2", "value2") {
                logger.info { "Info message 2" }

                withLoggingContext("key3", "value3") {
                    logger.info { "Info message 3" }
                }
            }
        }
    }

    @Benchmark
    open fun enabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        MDC.putCloseable("key1", "value1").use {
            logger.info("Info message 1")

            MDC.putCloseable("key2", "value2").use {
                logger.info("Info message 2")

                MDC.putCloseable("key3", "value3").use {
                    logger.info("Info message 3")
                }
            }
        }
    }

    @Benchmark
    open fun disabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        withLoggingContext("key1", "value1") {
            logger.debug { "Debug message 1" }

            withLoggingContext("key2", "value2") {
                logger.debug { "Debug message 2" }

                withLoggingContext("key3", "value3") {
                    logger.debug { "Debug message 3" }
                }
            }
        }
    }

    @Benchmark
    open fun disabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        MDC.putCloseable("key1", "value1").use {
            logger.debug("Debug message 1")

            MDC.putCloseable("key2", "value2").use {
                logger.debug("Debug message 2")

                MDC.putCloseable("key3", "value3").use {
                    logger.debug("Debug message 3")
                }
            }
        }
    }

    @Benchmark
    open fun coroutine_enabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        runNonSuspending {
            withContext(LoggingContext.with("key1", "value1")) {
                logger.info { "Info message 1" }

                withContext(LoggingContext.with("key2", "value2")) {
                    logger.info { "Info message 2" }

                    withContext(LoggingContext.with("key3", "value3")) {
                        logger.info { "Info message 3" }
                    }
                }
            }
        }
    }

    @Benchmark
    open fun coroutine_enabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        runNonSuspending {
            MDC.put("key1", "value1")
            withContext(MDCContext()) {
                logger.info("Info message 1")

                MDC.put("key2", "value2")
                withContext(MDCContext()) {
                    logger.info("Info message 2")

                    MDC.put("key3", "value3")
                    withContext(MDCContext()) {
                        logger.info("Info message 3")
                    }
                }
            }
        }
    }

    @Benchmark
    open fun coroutine_disabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        runNonSuspending {
            withContext(LoggingContext.with("key1", "value1")) {
                logger.debug { "Debug message 1" }

                withContext(LoggingContext.with("key2", "value2")) {
                    logger.debug { "Debug message 2" }

                    withContext(LoggingContext.with("key3", "value3")) {
                        logger.debug { "Debug message 3" }
                    }
                }
            }
        }
    }

    @Benchmark
    open fun coroutine_disabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        runNonSuspending {
            MDC.put("key1", "value1")
            withContext(MDCContext()) {
                logger.debug("Debug message 1")

                MDC.put("key2", "value2")
                withContext(MDCContext()) {
                    logger.debug("Debug message 2")

                    MDC.put("key3", "value3")
                    withContext(MDCContext()) {
                        logger.debug("Debug message 3")
                    }
                }
            }
        }
    }

}
