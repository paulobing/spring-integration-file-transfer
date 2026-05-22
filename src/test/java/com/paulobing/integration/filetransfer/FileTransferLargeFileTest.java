package com.paulobing.integration.filetransfer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class FileTransferLargeFileTest {

  abstract static class BaseTest {

    @Autowired protected FileTransferProperties props;

    @Value("${file.transfer.copyTimeoutMillis:60000}")
    protected long copyTimeoutMillis;

    protected Path sourceDir;
    protected Path targetDir;

    @BeforeEach
    void setUp() throws IOException {
      sourceDir = Paths.get(props.getSourceDir());
      targetDir = Paths.get(props.getTargetDir());
      Path filesToCopyDir = Paths.get("src/test/resources/files-to-copy");

      Files.createDirectories(targetDir);
      Files.createDirectories(sourceDir);

      // clear input directory (recursively)
      if (Files.exists(sourceDir)) {
        Files.walk(sourceDir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }

      // ensure input directory exists after cleanup
      Files.createDirectories(sourceDir);

      // copy files from test resources `files-to-copy` into the input directory
      if (Files.exists(filesToCopyDir)) {
        Files.list(filesToCopyDir)
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

    @Test
    void copiesLargeFileToOutput() throws IOException {
      Path filesToCopyDir = Paths.get("src/test/resources/files-to-copy");
      List<Path> expected =
          Files.list(filesToCopyDir).filter(Files::isRegularFile).collect(Collectors.toList());

      long deadline = System.currentTimeMillis() + copyTimeoutMillis;
      boolean allOk = false;
      while (System.currentTimeMillis() < deadline) {
        allOk = true;
        for (Path srcFile : expected) {
          Path targetFile = targetDir.resolve(srcFile.getFileName());
          if (!Files.exists(targetFile)) {
            allOk = false;
            break;
          }
          if (Files.size(targetFile) != Files.size(srcFile)) {
            allOk = false;
            break;
          }
        }
        if (allOk) break;
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }

      assertTrue(allOk, "Not all files were copied to target within 1 minute");
    }
  }

  @Nested
  @DirtiesContext
  @ActiveProfiles("java-dsl")
  class JavaDslProfileTest extends BaseTest {}

  @Nested
  @DirtiesContext
  @ActiveProfiles("xml")
  class XmlProfileTest extends BaseTest {}
}
