# Spring Integration File Transfer

Standalone Spring Boot application demonstrating file-transfer capabilities using Spring Integration with both Java DSL and XML-based configuration approaches.

---

# Overview

This project implements a polling-based asynchronous file-transfer pipeline using Spring Integration as the core messaging and orchestration framework.

The application continuously polls a configurable source directory, processes detected files through a Spring Integration channel-based pipeline, enriches messages with metadata, logs structured audit events, and writes the files to a configurable target directory.

The project intentionally provides equivalent implementations using:

- Java DSL
- XML-based Spring Integration configuration

This allows direct comparison between modern Java-based configuration and traditional XML integration flows.

Key Spring Integration concepts used:

- inbound channel adapters
- outbound gateways
- message channels
- pollers
- integration flows
- service activators
- metadata enrichment
- advice chains
- error channels

---

# Technology Stack

- Java 21
- Spring Boot 3.5.x
- Spring Integration 6.x
- Maven
- Lombok

---

# Features

- Java DSL implementation
- XML-based Spring Integration implementation
- Profile-based configuration switching
- Metadata enrichment using Spring Integration message headers
- Structured audit logging with transfer correlation IDs
- Configurable filename generation strategies
  - timestamp-based
  - UUID-based
- Error handling flow with advice chains
- Large-file integration testing
- Isolated temporary directories for integration tests
- Spring Integration channels, pollers, and service activators
- Automated asynchronous file transfer pipeline

---

# File Transfer Flow

The application implements the following integration pipeline:

```text
Input Directory
    ↓
Inbound Channel Adapter
    ↓
Metadata Enrichment
    ↓
Filename Transformation
    ↓
Audit Logging
    ↓
Outbound Gateway
    ↓
Success / Error Channels
    ↓
Output Directory
```

Each file is processed as a Spring Integration message flowing through channel-based components.

The flow is asynchronous and poller-driven.

---

# Running the Application

## Build

```bash
mvn clean install
```

---

## Default Profile (Java DSL)

The Java DSL implementation is configured as the default profile:

```properties
spring.profiles.active=java-dsl
```

Run:

```bash
mvn spring-boot:run
```

---

## XML Profile

Run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=xml
```

---

# Configuration

Example configuration:

```properties
spring.profiles.active=java-dsl

file.transfer.source-dir=./input
file.transfer.target-dir=./output
file.transfer.poll-interval-millis=2000

file.transfer.naming-strategy=timestamp
```

Configured directories are automatically created if they do not already exist.

---

# Filename Strategies

The application supports multiple generated filename strategies.

## Timestamp Strategy

Example generated filename:

```text
20260524_194214_hello.txt
```

---

## UUID Strategy

Example generated filename:

```text
8d8c8f2e-6f17-4ec2-ae14-f7a7c7f91f40_hello.txt
```

Enable using:

```properties
file.transfer.naming-strategy=uuid
```

---

# Metadata Enrichment

Each message is enriched with transfer metadata using Spring Integration headers.

Attached metadata includes:

- original filename
- generated filename
- file size
- ingestion timestamp
- transfer correlation ID

These headers are propagated throughout the integration flow and used for logging, auditing, and downstream processing.

---

# Audit Logging

Transfer events are logged as structured audit entries.

Example start event:

```text
JavaDSL AUDIT transferId=8d8c8f2e-6f17-4ec2-ae14-f7a7c7f91f40 \
event=file-transfer-started \
originalFilename=hello.txt \
generatedFilename=20260524_194214_hello.txt \
size=12345 \
ingestionTimestamp=2026-05-24T17:42:14Z
```

Example completion event:

```text
JavaDSL AUDIT transferId=8d8c8f2e-6f17-4ec2-ae14-f7a7c7f91f40 \
event=file-transfer-completed \
originalFilename=hello.txt \
generatedFilename=20260524_194214_hello.txt
```

The `transferId` acts as a correlation identifier, allowing all events related to a single file transfer to be traced throughout the pipeline.

---

# Auditing / Log Inspection

Transfer activity can be inspected using standard log tooling.

Example:

```bash
grep "transferId=" application.log
```

Or filter by event type:

```bash
grep "event=file-transfer-failed" application.log
```

Because logs use consistent `key=value` formatting, they are compatible with centralized log aggregation systems such as:

- ELK / Elastic Stack
- Splunk
- Datadog
- Grafana Loki

---

# Testing the File Transfer

Place a file into the configured input directory:

```text
input/hello.txt
```

The application will:

1. poll the source directory
2. detect the file
3. enrich transfer metadata
4. generate a transformed filename
5. log audit events
6. copy the file to the output directory

---

# Testing Strategy

Integration tests validate both Java DSL and XML implementations using isolated temporary directories created dynamically for each test context.

The test suite validates:

- successful file transfer
- large-file handling
- filename transformation
- metadata propagation
- profile parity between XML and Java DSL flows

Because the integration flow is asynchronous and poller-driven, tests use eventual-consistency polling with timeout-based assertions.

---

# Project Structure

```text
src/main
├── java
│   └── com.paulobing.integration.filetransfer
│       ├── config
│       ├── enricher
│       ├── flow
│       ├── service
│       ├── transformer
│       └── SpringIntegrationFileTransferApplication.java
└── resources
    ├── application.properties
    └── integration-file-transfer.xml
```

---

# Additional Documentation

- [XML Migration Guide](docs/XML_MIGRATION_GUIDE.md) — detailed explanation of the XML-based implementation, migration strategy to Java DSL, mapping decisions, and behavioral considerations
- [Architecture Overview](docs/ARCHITECTURE.md) — architecture overview, Spring Integration flow design, channels, profiles, logging, and error handling
- [Testing Strategy](docs/TESTING.md) — testing strategy, profile-based testing, large-file validation, and integration test considerations

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
