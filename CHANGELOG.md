Pre-release versions
====================

## Version 0.14

This version brings preparations for future extensions.

### Upgrade considerations

API & ABI of backend interfaces are not compatible with the previous version.

Usage of `this` in a lambda passed to `Logger.log()` and other similar methods
will not work anymore as `this` now refers to a `MessageBuilder` object.

## Version 0.13.1

### Fixes

  - Bring back erroneously removed non-inline
    `Logger.Companion.forClass()` extension to fix ABI compatibility.

    Now it is deprecated with `HIDDEN` level.

## Version 0.13

### New features

  - `LoggingRequest` is accessible to SLF4J implementation via special `Marker`
  - `LoggingRequest` now contains a `MessageDecor` instance

### Fixes & improvements

  - `PrefixMessageDecor` now ignores empty elements
  - Minimized size of inlined code in `LoggerFactory.currentClassLogger()`
    and `Logger.currentClass()` factory functions

### Deprecations

  - Removed deprecated `Logger.Companion` extensions

### Upgrade considerations

ABI & API compatibility is not preserved for backend interfaces.

The library should be upgraded to version 0.12 first.
All deprecation warnings should be fixed before upgrade to 0.13
to avoid facing with compilation errors.
