package benchmarks

import benchmarks.util.BenchmarkState
import benchmarks.util.runNonSuspending
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.openjdk.jmh.annotations.Benchmark
import org.slf4j.MDC
import ru.kontur.kinfra.logging.LoggingContext
import ru.kontur.kinfra.logging.withLoggingContext

open class ContextLogging {

    @Benchmark
    open fun single_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        withLoggingContext("key", "value") {
            logger.info { "Info message" }
        }
    }

    @Benchmark
    open fun single_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        MDC.putCloseable("key", "value").use {
            logger.info("Info message")
        }
    }

    @Benchmark
    open fun triple_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        withLoggingContext("key", "value") {
            logger.info { "Info message" }
            logger.warn { "Warning message" }
            logger.error { "Error message" }
        }
    }

    @Benchmark
    open fun triple_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        MDC.putCloseable("key", "value").use {
            logger.info("Info message")
            logger.warn("Warning message")
            logger.error("Error message")
        }
    }

    @Benchmark
    open fun disabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        withLoggingContext("key", "value") {
            logger.debug { "Debug message" }
        }
    }

    @Benchmark
    open fun disabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        MDC.putCloseable("key", "value").use {
            logger.debug("Debug message")
        }
    }

    @Benchmark
    open fun coroutine_single_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        runNonSuspending {
            withContext(LoggingContext.with("key", "value")) {
                logger.info { "Info message" }
            }
        }
    }

    @Benchmark
    open fun coroutine_single_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        runNonSuspending {
            MDC.put("key", "value")
            withContext(MDCContext()) {
                logger.info("Info message")
            }
        }
    }

    @Benchmark
    open fun coroutine_triple_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        runNonSuspending {
            withContext(LoggingContext.with("key", "value")) {
                logger.info { "Info message" }
                logger.warn { "Warning message" }
                logger.error { "Error message" }
            }
        }
    }

    @Benchmark
    open fun coroutine_triple_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        runNonSuspending {
            MDC.put("key", "value")
            withContext(MDCContext()) {
                logger.info("Info message")
                logger.warn("Warning message")
                logger.error("Error message")
            }
        }
    }

    @Benchmark
    open fun coroutine_disabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        runNonSuspending {
            withContext(LoggingContext.with("key", "value")) {
                logger.debug { "Debug message" }
            }
        }
    }

    @Benchmark
    open fun coroutine_disabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        runNonSuspending {
            MDC.put("key", "value")
            withContext(MDCContext()) {
                logger.debug("Debug message")
            }
        }
    }

}
