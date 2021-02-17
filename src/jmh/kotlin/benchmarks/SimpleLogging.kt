package benchmarks

import benchmarks.util.BenchmarkState
import org.openjdk.jmh.annotations.Benchmark

open class SimpleLogging {

    @Benchmark
    open fun enabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        logger.info { "Info message" }
    }

    @Benchmark
    open fun enabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        logger.info("Info message")
    }

    @Benchmark
    open fun disabled_kinfra(state: BenchmarkState) {
        val logger = state.kinfra

        logger.debug { "Debug message" }
    }

    @Benchmark
    open fun disabled_slf4j(state: BenchmarkState) {
        val logger = state.slf4j

        logger.debug("Debug message")
    }

}

