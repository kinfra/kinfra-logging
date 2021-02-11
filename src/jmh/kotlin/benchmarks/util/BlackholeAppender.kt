package benchmarks.util

import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.encoder.Encoder
import org.openjdk.jmh.infra.Blackhole
import org.slf4j.MDC

class BlackholeAppender<E> : UnsynchronizedAppenderBase<E>() {

    lateinit var encoder: Encoder<E>

    override fun append(eventObject: E) {
        blackhole.consume(MDC.getCopyOfContextMap())
        blackhole.consume(encoder.encode(eventObject))
    }

    companion object {

        lateinit var blackhole: Blackhole

    }

}
