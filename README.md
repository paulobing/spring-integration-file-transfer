# Spring Integration File Transfer

Standalone Spring Boot application demonstrating file transfer capabilities using Spring Integration with both Java DSL and XML-based configuration approaches.

---

# Overview

This project implements a file-transfer integration pipeline using Spring Integration as the core messaging and orchestration framework.

The application continuously polls a configurable source directory, processes detected files through a Spring Integration channel-based pipeline, logs transfer activity, and writes the files to a configurable target directory.

Key Spring Integration concepts used:

- inbound channel adapters
- outbound gateways
- message channels
- pollers
- integration flows
- service activators
- error handling/advice chains

---

# Technology Stack

- Java 21
- Spring Boot 3.5.x
- Spring Integration 6.x
- Maven
- Lombok

---

# Core Requirements Coverage

## Standalone Spring Boot Application

Implemented as a standalone Spring Boot application using Java 21 and Maven.

---

## Spring Integration Backbone

Spring Integration is used as the primary integration and messaging framework.

The application is built around:

- IntegrationFlow
- Spring Integration channels
- pollers
- file adapters/gateways

---

## file:inbound-channel-adapter

Implemented using:

- `FileReadingMessageSource` in Java DSL
- `int-file:inbound-channel-adapter` in XML configuration

Files are continuously polled from a configurable source directory.

---

## file:outbound-gateway

Implemented using:

- `Files.outboundGateway(...)` in Java DSL
- `int-file:outbound-gateway` in XML configuration

Files are written to a configurable target directory.

---

## Spring Integration Channels

Dedicated channels connect the inbound adapter and outbound gateway into a message-driven processing pipeline.

Pipeline flow:

```text
Source Directory
    ->
Inbound Adapter
    ->
Message Channel
    ->
Pre-processing / Logging
    ->
Outbound Gateway
    ->
Target Directory
```

---

# Additional Challenge - XML-Based Configuration

An equivalent XML-based Spring Integration implementation was added alongside the Java DSL implementation.

Both implementations are behaviorally equivalent and can be activated independently using Spring Profiles.

---

## Supported Profiles

### Java DSL (default)

Profile:

```properties
spring.profiles.active=java-dsl
```

or:

```bash
mvn spring-boot:run
```

Uses:

- `IntegrationFlow`
- Java-based Spring configuration
- DSL-based adapters, channels, and handlers

Main configuration:

```text
FileTransferFlow.java
```

---

### XML Configuration

Run with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=xml
```

Uses:

- Spring Integration XML namespaces
- XML-defined channels, adapters, pollers, and gateways

Main configuration:

```text
integration-file-transfer.xml
```

Imported through:

```java
@ImportResource("classpath:integration-file-transfer.xml")
```

---

# XML → Java DSL Migration Strategy

The XML implementation was intentionally designed to map closely to the Java DSL implementation.

## Key Mapping Decisions

| XML Component                        | Java DSL Equivalent                          |
| ------------------------------------ | -------------------------------------------- |
| `<int-file:inbound-channel-adapter>` | `IntegrationFlow.from(...)`                  |
| `<int:channel>`                      | `MessageChannel` bean                        |
| `<int:service-activator>`            | `.handle(...)`                               |
| `<int-file:outbound-gateway>`        | `Files.outboundGateway(...)`                 |
| `<int:poller>`                       | `PollerMetadata` / `Pollers.fixedDelay(...)` |
| XML bean references                  | Java bean injection                          |

---

## Migration Steps

1. Convert XML channels into `MessageChannel` beans
2. Replace XML adapters and gateways with Java DSL equivalents
3. Convert service activators into `.handle(...)` DSL steps
4. Move poller configuration into Java configuration beans
5. Replace XML placeholders with `@ConfigurationProperties`
6. Validate flow behavior and message routing after migration

---

## Behavioral Considerations

- XML configuration is more declarative and schema-driven
- Java DSL provides stronger IDE support and type safety
- Gateway reply semantics must be handled carefully in both implementations
- Bean naming consistency is important during migration

Both implementations were kept behaviorally equivalent to avoid functional differences during migration.

---

# Configuration

Example configuration:

```properties
spring.profiles.active=java-dsl

file.transfer.source-dir=./input
file.transfer.target-dir=./output
file.transfer.poll-interval-millis=5000
```

The application automatically creates configured directories if they do not already exist.

---

# Running the Application

## Build

```bash
mvn clean install
```

---

## Run Default Java DSL Profile

```bash
mvn spring-boot:run
```

---

## Run XML Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=xml
```

---

# Testing the File Transfer

The application automatically creates the configured input and output directories if they do not already exist.

By default:

```text
input/
output/
```

Place a file into the input directory:

```text
input/hello.txt
```

The application will:

1. poll the source directory
2. detect the file
3. log the transfer event
4. write the file to the output directory

---

# Project Structure

```text
src/main
├── java
│   └── com.paulobing.integration.filetransfer
│       ├── config
│       │   └── XmlIntegrationConfig.java
│       ├── flow
│       │   └── FileTransferFlow.java
│       ├── service
│       │   └── FileTransferService.java
│       └── SpringIntegrationFileTransferApplication.java
└── resources
    ├── application.properties
    └── integration-file-transfer.xml
```

---

# Limitations

- folders inside the source directory are ignored
- files are processed sequentially
- tested successfully with files up to 1.5 GB

---

# Future Improvements

- asynchronous file processing
- configurable retry strategies
- persistent metadata store for duplicate prevention
- structured audit logging
- REST endpoint exposing transfer history/status
- metrics and monitoring integration

---

# Notes on AI Assistance

AI-assisted development tools were used during implementation, including GitHub Copilot and ChatGPT.

These tools were primarily used to:

- accelerate boilerplate generation
- explore Spring Integration DSL alternatives
- validate framework compatibility
- resolve API/version compatibility issues
- discuss architectural and integration design decisions

All generated code and configuration were manually reviewed, adjusted, validated, and tested during implementation.
