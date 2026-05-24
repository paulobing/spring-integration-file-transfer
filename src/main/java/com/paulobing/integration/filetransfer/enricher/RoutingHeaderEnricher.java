package com.paulobing.integration.filetransfer.enricher;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutingHeaderEnricher {

  private final FileTransferProperties props;

  public Message<File> enrich(Message<File> message) {
    File file = message.getPayload();
    String route = determineRoute(file);

    return MessageBuilder.fromMessage(message)
        .setHeader(FileHeaders.ROUTE_TARGET, route)
        .setHeader(FileHeaders.INPUT_DIRECTORY, file.getParent())
        .setHeader(FileHeaders.OUTPUT_DIRECTORY, props.getTargetDir() + "/" + route)
        .build();
  }

  private String determineRoute(File file) {

    if (file.length() > props.getLargeFileThresholdBytes()) {
      return props.getLargeFileDir();
    }

    String extension = getExtension(file.getName());

    return props.getCategory().values().stream()
        .filter(category -> category.getExtensions().contains(extension))
        .map(FileTransferProperties.CategoryConfig::getDir)
        .findFirst()
        .orElse("default");
  }

  private String getExtension(String filename) {

    int index = filename.lastIndexOf('.');

    if (index == -1) {
      return "";
    }

    return filename.substring(index + 1).toLowerCase();
  }
}
