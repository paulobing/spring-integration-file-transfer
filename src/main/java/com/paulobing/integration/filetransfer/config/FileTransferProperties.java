package com.paulobing.integration.filetransfer.config;

import com.paulobing.integration.filetransfer.shared.FileNamingStrategy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "file.transfer")
@Validated
@Data
public class FileTransferProperties {

  private FileNamingStrategy namingStrategy = FileNamingStrategy.TIMESTAMP;

  @NotBlank private String sourceDir;

  @NotBlank private String targetDir;

  @Min(1)
  private long pollIntervalMillis;

  @Min(1)
  private long largeFileThresholdBytes;

  @NotBlank private String largeFileDir;

  private Map<String, CategoryConfig> category = new HashMap<>();

  @Data
  public static class CategoryConfig {

    private List<String> extensions;

    private String dir;
  }
}
