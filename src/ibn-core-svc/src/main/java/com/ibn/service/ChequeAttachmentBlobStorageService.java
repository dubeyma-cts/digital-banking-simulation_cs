package com.ibn.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
public class ChequeAttachmentBlobStorageService {

    private final String connectionString;
    private final String containerName;

    private volatile BlobContainerClient blobContainerClient;

    public ChequeAttachmentBlobStorageService(
            @Value("${app.storage.azure.connection-string:}") String connectionString,
            @Value("${app.storage.azure.container-name:cheque-attachments}") String containerName) {
        this.connectionString = safeTrim(connectionString);
        this.containerName = safeTrim(containerName).isEmpty() ? "cheque-attachments" : safeTrim(containerName);
    }

    public boolean isEnabled() {
        return !connectionString.isEmpty();
    }

    public String upload(UUID chequeDepositId, String attachmentName, byte[] content) {
        if (chequeDepositId == null || content == null || content.length == 0) {
            return "";
        }

        BlobContainerClient containerClient = getOrCreateContainerClient();
        String blobName = buildBlobName(chequeDepositId, attachmentName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(new ByteArrayInputStream(content), content.length, true);
        return blobClient.getBlobUrl();
    }

    private BlobContainerClient getOrCreateContainerClient() {
        BlobContainerClient cached = blobContainerClient;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (blobContainerClient == null) {
                BlobContainerClient created = new BlobContainerClientBuilder()
                        .connectionString(connectionString)
                        .containerName(containerName)
                        .buildClient();
                created.createIfNotExists();
                blobContainerClient = created;
            }
            return blobContainerClient;
        }
    }

    private String buildBlobName(UUID chequeDepositId, String attachmentName) {
        String safeName = safeTrim(attachmentName);
        if (safeName.isEmpty()) {
            safeName = "attachment.bin";
        }
        safeName = safeName.replace("\\", "_").replace("/", "_").replace("..", "_");
        return "cheque-deposit/" + chequeDepositId.toString() + "/" + safeName;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
