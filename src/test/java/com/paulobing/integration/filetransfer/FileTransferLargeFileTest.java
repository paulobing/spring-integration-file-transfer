package com.paulobing.integration.filetransfer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

class FileTransferLargeFileTest {

  abstract static class BaseTest {

    @Autowired protected FileTransferProperties props;

    @Value("${file.transfer.copyTimeoutMillis:60000}")
    protected long copyTimeoutMillis;

    @TempDir static Path tempDir;

    protected static Path sourceDir;
    protected static Path targetDir;

    @Test
    void copiesLargeFileToOutput() throws Exception {

      /*
       * The file transfer flow is asynchronous because files are consumed
       * by a polling inbound adapter running on a scheduler thread.
       *
       * This test therefore uses eventual-consistency polling:
       * it repeatedly verifies the expected state until either:
       *
       * - all files are transferred successfully
       * - or the configured timeout is reached
       *
       * The short sleep prevents aggressive busy-waiting while still
       * keeping the test responsive.
       */

      List<Path> expectedFiles = getExpectedFiles();

      long deadline = System.currentTimeMillis() + copyTimeoutMillis;

      AssertionError lastFailure = null;

      while (System.currentTimeMillis() < deadline) {

        try {

          for (Path expectedFile : expectedFiles) {
            assertFileTransferred(expectedFile);
          }

          return;

        } catch (AssertionError e) {

          lastFailure = e;

          // Small backoff to avoid busy-waiting while the async poller runs.
          Thread.sleep(250);
        }
      }

      String targetContents;

      try (var files = Files.list(targetDir)) {
        targetContents =
            files.map(path -> path.getFileName().toString()).sorted().toList().toString();
      }

      throw new AssertionError(
          """
          File transfer did not complete within timeout.

          Naming strategy: %s
          Source directory: %s
          Target directory: %s
          Target contents: %s

          Last observed failure:
          %s
          """
              .formatted(
                  props.getNamingStrategy(),
                  sourceDir,
                  targetDir,
                  targetContents,
                  lastFailure != null ? lastFailure.getMessage() : "unknown"));
    }

    private void assertFileTransferred(Path originalFile) throws IOException {

      String expectedRegex = buildExpectedFilenameRegex(originalFile);

      try (var files = Files.list(targetDir)) {

        List<Path> matchingFiles =
            files
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().matches(expectedRegex))
                .toList();

        assertTrue(
            !matchingFiles.isEmpty(),
            "No transformed file found for " + originalFile.getFileName());

        Path transferredFile = matchingFiles.getFirst();

        assertTrue(
            Files.size(transferredFile) == Files.size(originalFile),
            "Transferred file size mismatch for " + originalFile.getFileName());
      }
    }

    private String buildExpectedFilenameRegex(Path originalFile) {

      String originalName = originalFile.getFileName().toString();

      return switch (props.getNamingStrategy()) {
        case TIMESTAMP -> "^\\d{8}_\\d{6}_" + java.util.regex.Pattern.quote(originalName) + "$";
        case UUID -> "^[0-9a-fA-F\\-]{36}_" + java.util.regex.Pattern.quote(originalName) + "$";
      };
    }

    private List<Path> getExpectedFiles() throws IOException {

      Path filesToCopyDir = Paths.get("src/test/resources/files-to-copy");

      try (var files = Files.list(filesToCopyDir)) {
        return files.filter(Files::isRegularFile).toList();
      }
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) throws IOException {

      sourceDir = Files.createDirectory(tempDir.resolve("input"));
      targetDir = Files.createDirectory(tempDir.resolve("output"));

      registry.add("file.transfer.source-dir", () -> sourceDir.toString());
      registry.add("file.transfer.target-dir", () -> targetDir.toString());
    }

    @BeforeEach
    void setUp() throws IOException {
      copyTestFiles();
    }

    private void copyTestFiles() throws IOException {

      Path filesToCopyDir = Paths.get("src/test/resources/files-to-copy");

      if (!Files.exists(filesToCopyDir)) {
        return;
      }

      try (var files = Files.list(filesToCopyDir)) {

        files
            .filter(Files::isRegularFile)
            .forEach(
                src -> {
                  try {
                    Files.copy(
                        src,
                        sourceDir.resolve(src.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                });
      }
    }
  }

  /*
   * Intentionally using explicit profile × naming-strategy test classes.
   *
   * Current matrix size is small and stable:
   * - 2 integration implementations (java-dsl, xml)
   * - 2 naming strategies (timestamp, uuid)
   *
   * This results in only 4 Spring contexts and keeps the test structure:
   * - explicit
   * - readable
   * - easy to debug
   * - easy to reason about during evaluation/interviews
   *
   * If the configuration matrix grows significantly in the future
   * (additional strategies, profiles, routing modes, etc.),
   * a parameterized/context-driven testing approach would become preferable.
   */

  @Nested
  @SpringBootTest
  @DirtiesContext
  @ActiveProfiles("java-dsl")
  @TestPropertySource(properties = {"file.transfer.naming-strategy=timestamp"})
  class JavaDslTimestampTest extends BaseTest {}

  @Nested
  @SpringBootTest
  @DirtiesContext
  @ActiveProfiles("java-dsl")
  @TestPropertySource(properties = "file.transfer.naming-strategy=uuid")
  class JavaDslUuidTest extends BaseTest {}

  @Nested
  @SpringBootTest
  @DirtiesContext
  @ActiveProfiles("xml")
  @TestPropertySource(properties = "file.transfer.naming-strategy=timestamp")
  class XmlTimestampTest extends BaseTest {}

  @Nested
  @SpringBootTest
  @DirtiesContext
  @ActiveProfiles("xml")
  @TestPropertySource(properties = "file.transfer.naming-strategy=uuid")
  class XmlUuidTest extends BaseTest {}
}
