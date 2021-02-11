package benchmarks.util

import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import org.slf4j.LoggerFactory
import ru.kontur.kinfra.logging.Logger

@State(Scope.Benchmark)
open class BenchmarkState {

    val param: List<String> = listOf("foo", "bar", "baz")

    val kinfra = Logger.currentClass()

    val slf4j = LoggerFactory.getLogger(BenchmarkState::class.java)!!

    @Setup
    open fun injectBlackhole(blackhole: Blackhole) {
        BlackholeAppender.blackhole = blackhole
    }

}
