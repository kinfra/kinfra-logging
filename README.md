# kinfra-logging

A logging facade for Kotlin applications targeting JVM 11+.

Maven coordinates: `ru.kontur.kinfra:kinfra-logging`.

## Key concepts

### Logger

A `Logger` is an object responsible for logging messages from some part of the application (usually a class).

It is a final class provided by the library, that has a bunch of convenient methods for use from the application's code.

### LogLevel

`LogLevel` is an enumeration describing the importance of a log message.
There are 4 log levels:
* `DEBUG`  
  Detailed messages that can help in debugging an application's subsystem.
  Such messages allow a developer to trace the execution path or figure out the current state of the system.
  Logging of messages with this level is usually disabled, because it may noticeably affect
  application's performance and usage of persistent storage.

* `INFO`  
  General informational messages that describe activity of the application.
  Such messages usually give an insight into what is happening, but not why.

* `WARN`  
  Messages signaling potential problems or deviations of application's behavior.

* `ERROR`  
  Messages signaling failures that can affect the application's functionality.

### LoggerFactory

`LoggerFactory` is an entry point to the library that provides `Logger` instances to application code.

### LoggerBackend

`LoggerBackend` is used by `Logger` instances to actually perform logging.

It is a minimalistic interface to be implemented by custom extensions, bridges, or logging systems.

Implementation of `LoggerBackend` in default `LoggerFactory` delegates logging to [SLF4J](http://www.slf4j.org/).

### LoggingContext

`LoggingContext` contains supplementary data that should be logged with every log message.
It is an ordered set of key-value pairs (elements). Each element corresponds to the scope of processing of some entity.
A key of the element is the type of that entity, and its value is an identifier of the entity.

For example, consider a server processing requests.
It would be helpful to group all log messages related to a particular request,
since a lot of such requests may be processed concurrently.
If a request has some identifier, it can be turned into a `LoggingContext`.
Such context would contain an element with the key `requestId` and a value of that identifier.

Contexts are immutable and hierarchical. A nested context contains all elements of the outer one.

## Basic usage

In order to log messages, one should define a `Logger`:  // todo: questionable
```kotlin
val logger = Logger.currentClass()
```
This way, a `Logger` will be obtained from the default `LoggerFactory`.
The logger will be tied to the class declaring this property.

There is a `Logger.log(level: LogLevel, error: Throwable? = null, messageBuilder: () -> String)` method
that can be used for logging messages:
```kotlin
loggger.log(LogLevel.INFO) { "Some event occurred: $event" }
```
The `messageBuilder` lambda is evaluated only if the requested log level is enabled.

The `error` parameter can be used to log exceptions:
```kotlin
try {
    // do some operation
} catch (e: Exception) {
    logger.log(LogLevel.ERROR, e) { "Operation failed" }
}
```

The `log` method allows choosing log level at runtime, but usually it is not needed, so there are shortcut methods
for each level:
```kotlin
logger.debug { "Message with LogLevel.DEBUG" }
logger.info { "Message with LogLevel.INFO" }
logger.warn { "Message with LogLevel.WARN" }
logger.error { "Message with LogLevel.ERROR" }
```

To trace all the activity related to some entity, `LoggingContext` can be used.

For non-`suspend` code, there is `withLoggingContext` function:
```kotlin
val userId = 1234
withLoggingContext("userId", userId) {
    logger.info { "Performing some operation" }
}
```
If `LogLevel.INFO` is enabled in the underlying logging system, the log will contain a message:
```
[1234] Performing some operation
```
When using the default `LoggerFactory`, a log message will be prepended
with values of `LoggingContext` elements in the square brackets as shown above.

In `suspend` code, a combination of `withContext` (from kotlinx.coroutines library) and `LoggingContext.with` can be used:
```kotlin
val userId = 1234
withContext(LoggingContext.with("userId", userId)) {
    logger.info { "Performing some operation" }
}
```

Contexts can be nested:
```kotlin
val userId = 1234
withLoggingContext("userId", userId) {
    logger.info { "Performing some operation on user" }
    val orderId = 5678
    withLoggingContext("orderId", orderId) {
        logger.info { "Performing some operation on user's order" }
    }
}
```

Result:
```
[1234] Performing some operation on user
[1234] [5678] Performing some operation on user's order
```

It is incorrect to add to a context an element with a key that already present in the context.
Execution of the following code with lead to an `IllegalStateException`:
```kotlin
withLoggingContext("id", "foo") {
    withLoggingContext("id", "bar")
}
```
