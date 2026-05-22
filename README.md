# Spring Integration File Transfer

Standalone Spring Boot application demonstrating file transfer capabilities using Spring Integration and Java DSL configuration.

## Overview

This project implements a file-transfer integration pipeline using Spring Integration as the core messaging and orchestration framework.

The application continuously polls a configurable source directory, processes detected files through a Spring Integration channel-based pipeline, logs transfer activity, and writes the files to a configurable target directory.

The implementation follows a clean and minimal architecture while remaining aligned with Spring Integration concepts such as:

- inbound channel adapters
- outbound gateways
- message channels
- pollers
- integration flows

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

The application is implemented as a standalone Spring Boot application with Java 21 and Maven dependency management.

---

## Spring Integration Backbone

Spring Integration is used as the primary integration framework and messaging backbone.

The application is built around:

- `IntegrationFlow`
- message channels
- pollers
- file adapters/gateways

---

## file:inbound-channel-adapter

A `FileReadingMessageSource` is used to poll and read files from a configurable source directory.

The polling behavior is configured through a `PollerMetadata` bean using a configurable polling interval.

---

## file:outbound-gateway

A file outbound gateway is configured using Spring Integration's Java DSL support through:

```java
Files.outboundGateway(...)
```

The gateway writes processed files to the configured target directory.

---

## Spring Integration Channels

A dedicated `DirectChannel` is used to connect the inbound adapter and outbound gateway into a message-driven integration pipeline.

Pipeline flow:

```text
Source Directory
    ->
Inbound Channel Adapter
    ->
DirectChannel
    ->
Logging Handler
    ->
File Outbound Gateway
    ->
Target Directory
```

---

## Java DSL Configuration

All integration configuration is implemented using Spring Integration Java DSL and Java-based configuration.

No XML configuration is used for the core implementation.

---

# Configuration

Application properties are externalized using `@ConfigurationProperties`.

Example:

```properties
file.transfer.source-dir=./input
file.transfer.target-dir=./output
file.transfer.poll-interval-millis=5000
```

Validation is applied using Jakarta Bean Validation annotations.

---

# Running the Application

## Prerequisites

- Java 21+
- Maven 3.9+

---

## Build

```bash
mvn clean install
```

---

## Run

```bash
mvn spring-boot:run
```

---

# Testing the File Transfer

The application automatically creates the configured input and output directories if they do not already exist.

By default:

```text
input/
output/
```

Start the application and place a file inside the `input` directory.

Example:

```text
input/hello.txt
```

The application will:

1. poll the source directory
2. detect the file
3. log the transfer event
4. write the file to the `output` directory

---

# Logging

The application logs file processing events using SLF4J and Lombok logging support.

Example log entry:

```text
Processing file hello.txt
```

---

# Project Structure

```text
src/main/java
└── com.paulobing.integration.filetransfer
    ├── config
    │   └── FileTransferProperties.java
    ├── flow
    │   └── FileTransferFlow.java
    └── SpringIntegrationFileTransferApplication.java
```

---

# Limitations

- Folders within the source directory are not processed
- Tested up to 1.5GB file successfully

---

# Future implementations / Nice to have

- async execution of file transfer instead of one by one
- output log to a file in one of the directories besides only stdout
- add file transfer status (file, size, success/fail, date/time) to a table and expose an endpoint with this data

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
