package benchmarks.util

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn

fun runNonSuspending(block: suspend () -> Unit) {
    val completion = object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = Dispatchers.Unconfined

        override fun resumeWith(result: Result<Unit>) {
            error("Should not suspend")
        }
    }

    val result = block.startCoroutineUninterceptedOrReturn(completion)
    check(result == Unit)
}
