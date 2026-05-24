package com.paulobing.integration.filetransfer.enricher;

import static org.assertj.core.api.Assertions.assertThat;

import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class FileMetadataEnricherTest {

  @TempDir Path tempDir;

  private final FileMetadataEnricher enricher = new FileMetadataEnricher();

  @Nested
  class Enrich {

    @Test
    void addsOriginalFilename() throws IOException {

      File file = createFile("report.txt", 100);

      Message<?> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ORIGINAL_FILENAME)).isEqualTo("report.txt");
    }

    @Test
    void addsFileSize() throws IOException {

      File file = createFile("report.txt", 2048);

      Message<?> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.FILE_SIZE)).isEqualTo(2048L);
    }

    @Test
    void addsTransferId() throws IOException {

      File file = createFile("report.txt", 100);

      Message<?> result = enricher.enrich(buildMessage(file));

      Object transferId = result.getHeaders().get(FileHeaders.TRANSFER_ID);

      assertThat(transferId).isInstanceOf(String.class);

      assertThat((String) transferId).isNotBlank();
    }

    @Test
    void addsIngestionTimestamp() throws IOException {

      File file = createFile("report.txt", 100);

      Instant before = Instant.now();

      Message<?> result = enricher.enrich(buildMessage(file));

      Instant after = Instant.now();

      Instant timestamp = (Instant) result.getHeaders().get(FileHeaders.INGESTION_TIMESTAMP);

      assertThat(timestamp).isBetween(before, after);
    }
  }

  private Message<File> buildMessage(File file) {
    return MessageBuilder.withPayload(file).build();
  }

  private File createFile(String name, int size) throws IOException {

    Path path = tempDir.resolve(name);

    Files.write(path, new byte[size]);

    return path.toFile();
  }
}
