package com.paulobing.integration.filetransfer.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class FileNameTransformerTest {

  @Nested
  class TransformWithTimestampStrategy {

    @Test
    void prefixesFileWithTimestamp() {

      FileTransferProperties props = new FileTransferProperties();
      props.setNamingStrategy(
          com.paulobing.integration.filetransfer.shared.FileNamingStrategy.TIMESTAMP);

      FileNameTransformer transformer = new FileNameTransformer(props);

      File file = new File("document.txt");

      Message<File> message = MessageBuilder.withPayload(file).build();

      Message<?> result = transformer.transform(message);

      String generatedFilename = (String) result.getHeaders().get(FileHeaders.GENERATED_FILENAME);

      assertThat(generatedFilename).matches("^\\d{8}_\\d{6}_document\\.txt$");
    }
  }

  @Nested
  class TransformWithUuidStrategy {

    @Test
    void prefixesFileWithUuid() {

      FileTransferProperties props = new FileTransferProperties();
      props.setNamingStrategy(
          com.paulobing.integration.filetransfer.shared.FileNamingStrategy.UUID);

      FileNameTransformer transformer = new FileNameTransformer(props);

      File file = new File("document.txt");

      Message<File> message = MessageBuilder.withPayload(file).build();

      Message<?> result = transformer.transform(message);

      String generatedFilename = (String) result.getHeaders().get(FileHeaders.GENERATED_FILENAME);

      assertThat(generatedFilename).matches("^[0-9a-fA-F\\-]{36}_document\\.txt$");
    }
  }
}
