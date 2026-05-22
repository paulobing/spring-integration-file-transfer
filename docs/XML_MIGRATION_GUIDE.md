# XML to Java DSL Migration Guide

This document describes the XML-based Spring Integration implementation and the migration strategy used to convert it into an equivalent Java DSL implementation.

---

# Goal

The objective was to produce a Spring Integration XML configuration equivalent to the Java DSL implementation while preserving:

- behavior
- message routing
- polling semantics
- file handling
- logging
- error handling

Both implementations are behaviorally equivalent and selectable through Spring Profiles.

---

# XML Implementation Overview

The XML implementation uses:

- `int-file:inbound-channel-adapter`
- `int-file:outbound-gateway`
- `int:channel`
- `int:service-activator`
- `int:poller`
- request-handler advice chains

Main XML configuration file:

```text
integration-file-transfer.xml
```

---

# XML → Java DSL Mapping

| XML Component                        | Java DSL Equivalent                          |
| ------------------------------------ | -------------------------------------------- |
| `<int-file:inbound-channel-adapter>` | `IntegrationFlow.from(...)`                  |
| `<int:channel>`                      | `MessageChannel` bean                        |
| `<int:service-activator>`            | `.handle(...)`                               |
| `<int-file:outbound-gateway>`        | `Files.outboundGateway(...)`                 |
| `<int:poller>`                       | `PollerMetadata` / `Pollers.fixedDelay(...)` |
| XML bean references                  | Java bean injection                          |
| Advice chains                        | `.advice(...)`                               |

---

# Migration Steps

## 1. Convert Channels

XML channels:

```xml
<int:channel id="fileTransferChannel" />
```

became:

```java
@Bean
public MessageChannel fileTransferChannel() {
    return new DirectChannel();
}
```

---

## 2. Replace Inbound Adapter

XML:

```xml
<int-file:inbound-channel-adapter ... />
```

became:

```java
IntegrationFlow.from(fileReadingMessageSource, ...)
```

using `FileReadingMessageSource`.

---

## 3. Replace Service Activators

XML:

```xml
<int:service-activator ... />
```

became:

```java
.handle(...)
```

inside the Java DSL flow.

---

## 4. Replace Outbound Gateway

XML:

```xml
<int-file:outbound-gateway ... />
```

became:

```java
Files.outboundGateway(...)
```

---

## 5. Move Poller Configuration

XML pollers:

```xml
<int:poller fixed-delay="5000" />
```

became:

```java
PollerMetadata
```

and:

```java
PeriodicTrigger
```

configuration.

---

## 6. Replace XML Property Placeholders

XML placeholders:

```properties
${file.transfer.source-dir}
```

were migrated into:

```java
@ConfigurationProperties
```

based configuration.

---

# Behavioral Considerations

## XML Configuration

Advantages:

- declarative
- schema-driven
- concise for simple integrations

Tradeoffs:

- weaker IDE navigation
- weaker compile-time safety
- more XML-specific syntax nuances

---

## Java DSL Configuration

Advantages:

- stronger type safety
- easier refactoring
- easier debugging
- modern Spring Integration style

Tradeoffs:

- slightly more verbose
- requires Java familiarity

---

# Migration Notes

Special attention was required for:

- outbound gateway reply semantics
- advice chain mapping
- channel naming consistency
- profile isolation between XML and Java DSL
- Spring Integration XML schema compatibility

---

# Final Result

Both implementations were kept behaviorally equivalent and are selectable independently through Spring Profiles:

- `java-dsl`
- `xml`

This allows migration validation without functional differences between implementations.
