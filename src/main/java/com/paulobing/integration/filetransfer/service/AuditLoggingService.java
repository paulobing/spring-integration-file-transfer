package com.paulobing.integration.filetransfer.service;

import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditLoggingService {

  private final String logPrefix;

  public AuditLoggingService(Environment environment) {
    String[] profiles = environment.getActiveProfiles();
    this.logPrefix = profiles.length > 0 ? profiles[0] : "default";
  }

  public Message<?> logTransferStarted(Message<?> message) {

    validateFile((File) message.getPayload());

    log.info(
        "{} AUDIT transferId={} event={} originalFilename={} generatedFilename={} "
            + "size={} ingestionTimestamp={} inputDirectory={} outputDirectory={}",
        logPrefix,
        message.getHeaders().get(FileHeaders.TRANSFER_ID),
        "file-transfer-started",
        message.getHeaders().get(FileHeaders.ORIGINAL_FILENAME),
        message.getHeaders().get(FileHeaders.GENERATED_FILENAME),
        message.getHeaders().get(FileHeaders.FILE_SIZE),
        message.getHeaders().get(FileHeaders.INGESTION_TIMESTAMP),
        message.getHeaders().get(FileHeaders.INPUT_DIRECTORY),
        message.getHeaders().get(FileHeaders.OUTPUT_DIRECTORY));

    return message;
  }

  public void logTransferCompleted(Message<?> message) {

    log.info(
        "{} AUDIT transferId={} event={} originalFilename={} generatedFilename={} "
            + "inputDirectory={} outputDirectory={}",
        logPrefix,
        message.getHeaders().get(FileHeaders.TRANSFER_ID),
        "file-transfer-completed",
        message.getHeaders().get(FileHeaders.ORIGINAL_FILENAME),
        message.getHeaders().get(FileHeaders.GENERATED_FILENAME),
        message.getHeaders().get(FileHeaders.INPUT_DIRECTORY),
        message.getHeaders().get(FileHeaders.OUTPUT_DIRECTORY));
  }

  private void validateFile(File file) {
    if (file.isDirectory()) {
      throw new IllegalArgumentException("folders are not supported - folder: " + file.getName());
    }
  }
}
