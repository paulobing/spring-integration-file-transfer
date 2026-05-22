# Architecture Overview

This document describes the application's architecture and Spring Integration flow design.

---

# High-Level Flow

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

# Main Components

## Inbound Adapter

Responsible for polling files from the configured source directory.

Implemented using:

- `FileReadingMessageSource`
- `int-file:inbound-channel-adapter`

---

## Message Channels

Spring Integration channels connect processing stages inside the pipeline.

Primary channels:

- fileTransferChannel
- fileTransferErrorChannel

---

## Pre-processing

The flow performs:

- logging
- validation
- error handling preparation

before file transfer execution.

---

## Outbound Gateway

Responsible for writing files to the configured target directory.

Implemented using:

- `Files.outboundGateway(...)`
- `int-file:outbound-gateway`

---

# Error Handling

The application includes:

- dedicated error channels
- request-handler advice chains
- structured logging

This allows failed transfers to be isolated and logged independently.

---

# Profile-Based Configuration

Two independent implementations are available:

| Profile  | Implementation         |
| -------- | ---------------------- |
| java-dsl | Java DSL               |
| xml      | XML Spring Integration |

This enables:

- side-by-side comparison
- migration validation
- implementation parity testing

---

# Logging

Structured logs are emitted during:

- transfer start
- transfer success
- transfer failure

including:

- filename
- file size
- source directory
- target directory

---

# Large File Support

The implementation was tested successfully with files up to approximately 1.5 GB.

The application processes files sequentially to reduce concurrency-related file handling issues.

---

# Future Improvements

Potential enhancements include:

- asynchronous processing
- retry policies
- persistent metadata store
- monitoring and metrics
- REST API exposing transfer history
- distributed locking support
