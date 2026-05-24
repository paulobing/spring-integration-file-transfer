package com.paulobing.integration.filetransfer;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("java-dsl")
@Slf4j
class IncompleteFileTransferTest {

  private static final int WRITE_COUNT = 5;
  private static final long WRITE_INTERVAL_MILLIS = 2000;
  private static final long FILE_READY_AGE_MILLIS = 5000;
  private static final long TRANSFER_TIMEOUT_MILLIS = 15000;

  @TempDir static Path tempDir;

  static Path sourceDir;
  static Path targetDir;

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) throws Exception {

    sourceDir = Files.createDirectory(tempDir.resolve("input"));
    targetDir = Files.createDirectory(tempDir.resolve("output"));

    registry.add("file.transfer.source-dir", () -> sourceDir.toString());
    registry.add("file.transfer.target-dir", () -> targetDir.toString());

    registry.add("file.transfer.naming-strategy", () -> "timestamp");
    registry.add("file.transfer.poll-interval-millis", () -> "500");
    registry.add("file.transfer.file-ready-age-seconds", () -> "5");
  }

  @Test
  void doesNotConsumeFileUntilWriteCompletes() throws Exception {

    Files.createDirectories(targetDir.resolve("text"));

    Path file = sourceDir.resolve("large-file.txt");

    Thread writer = createSlowWriter(file);

    writer.start();

    /*
     * While the producer is still writing,
     * the integration flow must NOT consume the file.
     */
    for (int i = 1; i <= WRITE_COUNT; i++) {

      Thread.sleep(WRITE_INTERVAL_MILLIS);

      long currentSize = Files.size(file);

      log.info(
          "Waiting for file stabilization - currentSize={} bytes transferred={}",
          currentSize,
          hasTransferredFile());

      assertFalse(hasTransferredFile(), "File should not be transferred while still being written");
    }

    writer.join();

    log.info(
        "Producer finished writing. Waiting {} ms for stabilization window.",
        FILE_READY_AGE_MILLIS);

    Thread.sleep(FILE_READY_AGE_MILLIS);

    awaitTransferredFile();

    log.info("File transferred successfully after stabilization window.");
  }

  private Thread createSlowWriter(Path file) {

    return new Thread(
        () -> {
          try (OutputStream out = Files.newOutputStream(file)) {

            for (int i = 1; i <= WRITE_COUNT; i++) {

              out.write(new byte[1024]);
              out.flush();

              log.info("Producer wrote chunk {} of {}", i, WRITE_COUNT);

              Thread.sleep(WRITE_INTERVAL_MILLIS);
            }

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  private void awaitTransferredFile() throws Exception {

    long deadline = System.currentTimeMillis() + TRANSFER_TIMEOUT_MILLIS;

    while (System.currentTimeMillis() < deadline) {

      if (hasTransferredFile()) {
        return;
      }

      Thread.sleep(250);
    }

    throw new AssertionError(
        """
        File was not transferred after stabilization window elapsed.

        Source directory: %s
        Target directory: %s
        """
            .formatted(sourceDir, targetDir));
  }

  private boolean hasTransferredFile() throws Exception {

    Path routedDir = targetDir.resolve("text");

    if (!Files.exists(routedDir)) {
      return false;
    }

    try (var files = Files.list(routedDir)) {

      return files.anyMatch(path -> path.getFileName().toString().contains("large-file.txt"));
    }
  }
}
