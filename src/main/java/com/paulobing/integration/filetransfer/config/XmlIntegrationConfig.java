package com.paulobing.integration.filetransfer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("xml")
@ImportResource("classpath:integration-file-transfer.xml")
public class XmlIntegrationConfig {}
