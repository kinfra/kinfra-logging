package ru.kontur.kinfra.logging.backend

class CallerInfo(
    /**
     * The fully qualified name of the facade class called by user code.
     *
     * At the time of [LoggerBackend.log] call the stack contains following frames:
     *
     * 1. LoggerBackend.log() itself
     * 2. Custom LoggerBackend decorators (zero or more)
     * 3. Some library machinery (zero or more)
     * 4. One or more frames in facade class methods
     * 5. Logger usage in user code
     * 6. Other user code
     *
     * Location aware logging implementation should skip frames 1-4 and use the frame 5
     * as the actual place in user code where logger was called.
     *
     * At least one frame in some method of the class named [facadeClassName] must be on the stack
     * when the [LoggerBackend.log] is called.
     */
    val facadeClassName: String
)
