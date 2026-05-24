package com.paulobing.integration.filetransfer.enricher;

import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import java.time.Instant;
import java.util.UUID;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataEnricher {

  public Message<?> enrich(Message<?> message) {

    File file = (File) message.getPayload();
    return MessageBuilder.fromMessage(message)
        .setHeader(FileHeaders.ORIGINAL_FILENAME, file.getName())
        .setHeader(FileHeaders.FILE_SIZE, file.length())
        .setHeader(FileHeaders.INGESTION_TIMESTAMP, Instant.now())
        .setHeader(FileHeaders.TRANSFER_ID, UUID.randomUUID().toString())
        .build();
  }
}
