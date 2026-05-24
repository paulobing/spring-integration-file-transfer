package com.paulobing.integration.filetransfer.enricher;

import static org.assertj.core.api.Assertions.assertThat;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import com.paulobing.integration.filetransfer.shared.FileHeaders;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class RoutingHeaderEnricherTest {

  @TempDir Path tempDir;

  private final FileTransferProperties props = buildProperties();

  private final RoutingHeaderEnricher enricher = new RoutingHeaderEnricher(props);

  @Nested
  class Enrich {

    @Test
    void routesImageFilesToImageDirectory() throws IOException {

      File file = createFile("photo.jpg", 1024);

      Message<File> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ROUTE_TARGET)).isEqualTo("images");
    }

    @Test
    void routesTextFilesToTextDirectory() throws IOException {

      File file = createFile("notes.txt", 1024);

      Message<File> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ROUTE_TARGET)).isEqualTo("text");
    }

    @Test
    void routesLargeFilesToLargeFilesDirectory() throws IOException {

      File file = createFile("video.mp4", 10_000);

      Message<File> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ROUTE_TARGET)).isEqualTo("large-files");
    }

    @Test
    void routesUnknownExtensionsToDefaultDirectory() throws IOException {

      File file = createFile("archive.bin", 1024);

      Message<File> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ROUTE_TARGET)).isEqualTo("default");
    }

    @Test
    void handlesFilesWithoutExtension() throws IOException {

      File file = createFile("README", 1024);

      Message<File> result = enricher.enrich(buildMessage(file));

      assertThat(result.getHeaders().get(FileHeaders.ROUTE_TARGET)).isEqualTo("default");
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

  private FileTransferProperties buildProperties() {

    FileTransferProperties properties = new FileTransferProperties();

    properties.setLargeFileThresholdBytes(5000);
    properties.setLargeFileDir("large-files");

    FileTransferProperties.CategoryConfig image = new FileTransferProperties.CategoryConfig();
    image.setExtensions(List.of("jpg", "jpeg", "png", "gif"));
    image.setDir("images");

    FileTransferProperties.CategoryConfig text = new FileTransferProperties.CategoryConfig();
    text.setExtensions(List.of("txt", "log", "csv"));
    text.setDir("text");

    properties.setCategory(
        java.util.Map.of(
            "image", image,
            "text", text));

    return properties;
  }
}
