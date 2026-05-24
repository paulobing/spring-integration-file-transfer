package com.paulobing.integration.filetransfer.flow;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;
import com.paulobing.integration.filetransfer.enricher.FileMetadataEnricher;
import com.paulobing.integration.filetransfer.service.AuditLoggingService;
import com.paulobing.integration.filetransfer.shared.FileHeaders;
import com.paulobing.integration.filetransfer.transformer.FileNameTransformer;
import java.io.File;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.support.PeriodicTrigger;

@Profile("java-dsl")
@Configuration
@Slf4j
public class FileTransferFlow {
  private static final String LOG_PREFIX = "Java DSL";

  @Bean
  public MessageChannel fileTransferChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel fileTransferErrorChannel() {
    return new DirectChannel();
  }

  @Bean
  public FileReadingMessageSource fileReadingMessageSource(FileTransferProperties props) {
    FileReadingMessageSource source = new FileReadingMessageSource();
    source.setDirectory(new File(props.getSourceDir()));
    source.setFilter(new AcceptOnceFileListFilter<>());
    return source;
  }

  @Qualifier("fileOutboundGatewayHandler")
  @Bean
  public MessageHandler fileOutboundGatewayHandler(FileTransferProperties props) {
    return Files.outboundGateway(new File(props.getTargetDir()))
        .fileNameGenerator(
            message -> (String) message.getHeaders().get(FileHeaders.GENERATED_FILENAME))
        .getObject();
  }

  @Bean
  public Advice fileTransferFailureLoggingAdvice(MessageChannel fileTransferErrorChannel) {
    ExpressionEvaluatingRequestHandlerAdvice advice =
        new ExpressionEvaluatingRequestHandlerAdvice();
    advice.setTrapException(true);
    advice.setFailureChannel(fileTransferErrorChannel);
    return advice;
  }

  @Bean
  public IntegrationFlow fileTransferErrorFlow() {
    return IntegrationFlow.from(fileTransferErrorChannel())
        .handle(
            errorMsg -> {
              Object payload = errorMsg.getPayload();
              Object inputMessage = errorMsg.getHeaders().get("inputMessage");
              String fileName = null;

              if (inputMessage instanceof Message<?>) {
                Object inputPayload = ((Message<?>) inputMessage).getPayload();
                if (inputPayload instanceof File) {
                  fileName = ((File) inputPayload).getName();
                }
              }

              if (payload instanceof Throwable) {
                Throwable throwable = (Throwable) payload;
                String errorText = throwable.getMessage();
                if (errorText == null) {
                  errorText = throwable.getClass().getSimpleName();
                }
                if (fileName != null) {
                  log.error(
                      LOG_PREFIX
                          + " Transfer Files - failed processing file transfer of file {}: {}",
                      fileName,
                      errorText);
                } else {
                  log.error(
                      LOG_PREFIX + " Transfer Files - failed processing file transfer: {}",
                      errorText);
                }
              } else {
                log.error(
                    LOG_PREFIX + " Transfer Files - failed processing file transfer: {}", payload);
              }
            })
        .get();
  }

  @Bean
  public PollerMetadata poller(FileTransferProperties props) {
    PollerMetadata poller = new PollerMetadata();
    poller.setTrigger(new PeriodicTrigger(Duration.ofMillis(props.getPollIntervalMillis())));
    return poller;
  }

  @Bean
  public IntegrationFlow fileMoveFlow(
      FileReadingMessageSource fileReadingMessageSource,
      @Qualifier("fileOutboundGatewayHandler") MessageHandler fileOutboundGatewayHandler,
      FileMetadataEnricher fileMetadataEnricher,
      AuditLoggingService auditLoggingService,
      FileNameTransformer fileNameTransformer,
      PollerMetadata poller,
      Advice fileTransferFailureLoggingAdvice) {

    return IntegrationFlow.from(fileReadingMessageSource, config -> config.poller(poller))
        .channel(fileTransferChannel())
        .handle(fileMetadataEnricher, "enrich")
        .handle(fileNameTransformer, "transform")
        .handle(auditLoggingService, "logTransferStarted")
        .handle(fileOutboundGatewayHandler, e -> e.advice(fileTransferFailureLoggingAdvice))
        .handle(auditLoggingService, "logTransferCompleted")
        .nullChannel();
  }

  @Bean
  public Advice fileTransferLoggingAdvice(AuditLoggingService auditLoggingService) {
    ExpressionEvaluatingRequestHandlerAdvice advice =
        new ExpressionEvaluatingRequestHandlerAdvice();

    advice.setOnSuccessExpressionString("payload");
    advice.setSuccessChannelName("nullChannel");
    advice.setTrapException(true);
    return advice;
  }
}
