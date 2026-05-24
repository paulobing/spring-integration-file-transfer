package com.paulobing.integration.filetransfer.shared;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum FileNamingStrategy {
  TIMESTAMP {
    @Override
    public String generatePrefix() {
      return TIMESTAMP_FORMATTER.format(LocalDateTime.now());
    }
  },

  UUID {
    @Override
    public String generatePrefix() {
      return java.util.UUID.randomUUID().toString();
    }
  };

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  public abstract String generatePrefix();
}
