package com.paulobing.integration.filetransfer.config;

import com.paulobing.integration.filetransfer.shared.FileNamingStrategy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
}
