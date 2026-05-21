package com.paulobing.integration.filetransfer.flow;

import java.io.File;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.integration.dsl.IntegrationFlow;

import com.paulobing.integration.filetransfer.config.FileTransferProperties;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FileTransferFlow {

    @Bean
    public MessageChannel fileTransferChannel() {
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
        return Files
                .outboundGateway(new File(props.getTargetDir()))
                .getObject();
    }

    @Bean
    public PollerMetadata poller(FileTransferProperties props) {
        PollerMetadata poller = new PollerMetadata();
        poller.setTrigger(new PeriodicTrigger(Duration.ofMillis(props.getPollIntervalMillis())));
        return poller;
    }

    @Bean
    public IntegrationFlow fileMoveFlow(FileReadingMessageSource fileReadingMessageSource,
            @Qualifier("fileOutboundGatewayHandler") MessageHandler fileOutboundGatewayHandler,
            PollerMetadata poller) {
        return IntegrationFlow
                .from(fileReadingMessageSource, config -> config.poller(poller))
                .channel(fileTransferChannel())
                .handle(File.class, (file, headers) -> {
                    log.info("Processing file {}", file.getName());
                    return file;
                })
                .handle(fileOutboundGatewayHandler)
                .nullChannel();
    }
}
