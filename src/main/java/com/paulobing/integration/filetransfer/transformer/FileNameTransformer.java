package com.paulobing.integration.filetransfer.transformer;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class FileNameTransformer {
  private final FileTransferProperties properties;

  public FileNameTransformer(FileTransferProperties properties) {

    this.properties = properties;
  }

  public Message<?> transform(Message<?> message) {
    File file = (File) message.getPayload();
    String generatedFilename = generateFileName(file);

    return MessageBuilder.fromMessage(message)
        .setHeader(FileHeaders.GENERATED_FILENAME, generatedFilename)
        .build();
  }

  private String generateFileName(File file) {
    return properties.getNamingStrategy().generatePrefix() + "_" + file.getName();
  }
}
