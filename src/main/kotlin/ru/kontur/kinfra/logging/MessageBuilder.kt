package ru.kontur.kinfra.logging

/**
 * Receiver of lambdas passed to [Logger.log].
 *
 * It is a future extension point without any functionality for now.
 */
public abstract class MessageBuilder private constructor() {
    /*
     * Implementation considerations:
     * Beware that adding ANY public members to this class
     * may brake its API and semantic compatibility because
     * some users might use the same symbol from another scope inside message-producer lambda.
     */

    private class StubImpl : MessageBuilder()

    public companion object {

        @PublishedApi
        @JvmField
        internal val STUB: MessageBuilder = StubImpl()

    }

}
