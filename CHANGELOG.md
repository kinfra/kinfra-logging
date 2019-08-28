Pre-release versions
====================

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
