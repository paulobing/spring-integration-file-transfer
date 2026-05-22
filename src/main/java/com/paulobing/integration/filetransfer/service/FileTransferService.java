package com.paulobing.integration.filetransfer.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("fileTransferService")
public class FileTransferService {
    private static final String LOG_PREFIX = "XML";

    private final Logger log = LoggerFactory.getLogger(FileTransferService.class);

    public File preHandle(File file) {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("folders are not supported - folder: " + file.getName());
        }
        log.info(LOG_PREFIX + " Transfer Files - copying file {} (size: {} bytes)", file.getName(), file.length());
        return file;
    }

    public void postHandle(File file) {
        log.info(LOG_PREFIX + " Transfer Files - finished processing file transfer of file {}", file.getName());
    }

    public void handleError(Message<?> errorMsg) {
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
                log.error(LOG_PREFIX + " Transfer Files - failed processing file transfer of file {}: {}", fileName,
                        errorText);
            } else {
                log.error(LOG_PREFIX + " Transfer Files - failed processing file transfer: {}", errorText);
            }
        } else {
            log.error(LOG_PREFIX + " Transfer Files - failed processing file transfer: {}", payload);
        }
    }
}
