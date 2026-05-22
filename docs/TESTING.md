# Testing Strategy

This document describes the testing strategy used for validating the Spring Integration file transfer pipeline.

---

# Test Coverage

The project includes tests for:

- application context startup
- Java DSL profile
- XML profile
- file transfer validation
- large-file handling

---

# Profile Validation

Both integration implementations are tested independently:

- `java-dsl`
- `xml`

This ensures:

- behavioral equivalence
- migration consistency
- configuration correctness

---

# Large File Testing

Large file transfer tests validate:

- file existence after transfer
- file size consistency
- successful polling and processing

The tests use configurable timeouts to allow large file transfers to complete.

---

# Test Resources

Test resources are located under:

```text
src/test/resources
```

Files placed under:

```text
files-to-copy/
```

are copied into the configured input directory before execution.

---

# Spring Boot Testing

Tests use:

```java
@SpringBootTest
```

to validate the full Spring Integration pipeline behavior.

Profile-specific tests use:

```java
@ActiveProfiles(...)
```

to activate the desired integration implementation.

---

# Notes

Because Spring Integration pollers and schedulers remain active during tests, context isolation and profile separation were important to avoid test interference.
