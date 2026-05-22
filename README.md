# Spring Integration File Transfer

Standalone Spring Boot application demonstrating file transfer capabilities using Spring Integration using both Java DSL and XML-based configuration approaches.

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

# Features

- Java DSL implementation
- XML-based Spring Integration implementation
- Profile-based configuration switching
- Structured logging
- Error handling flow
- Large-file support testing
- Spring Integration channels and pollers
- Automated file transfer pipeline

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
```

Configured directories are automatically created if they do not already exist.

---

# Testing the File Transfer

Place a file into the configured input directory:

```text
input/hello.txt
```

The application will:

1. poll the source directory
2. detect the file
3. log the transfer event
4. copy the file to the output directory

---

# Project Structure

```text
src/main
├── java
│   └── com.paulobing.integration.filetransfer
│       ├── config
│       ├── flow
│       ├── service
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
