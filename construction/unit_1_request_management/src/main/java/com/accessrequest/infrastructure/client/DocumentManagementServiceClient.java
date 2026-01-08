package com.accessrequest.infrastructure.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Client for integrating with Document Management Service.
 * Provides methods to retrieve document information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentManagementServiceClient {

    private final RestTemplate restTemplate;

    @Value("${external.services.document-management.url:http://localhost:8082}")
    private String documentManagementServiceUrl;

    /**
     * Get document metadata by document ID.
     *
     * @param documentId the document ID
     * @return the document response
     */
    public DocumentResponse getDocumentMetadata(UUID documentId) {
        log.debug("Getting document metadata for document: {}", documentId);
        try {
            String url = documentManagementServiceUrl + "/api/v1/documents/" + documentId;
            DocumentResponse response = restTemplate.getForObject(url, DocumentResponse.class);
            log.debug("Document metadata retrieved: {}", documentId);
            return response;
        } catch (Exception e) {
            log.error("Error getting document metadata: {}", documentId, e);
            throw new ExternalServiceException("Failed to get document metadata", e);
        }
    }

    /**
     * List all documents for a request.
     *
     * @param requestId the request ID
     * @return list of document responses
     */
    public List<DocumentResponse> listDocumentsForRequest(UUID requestId) {
        log.debug("Listing documents for request: {}", requestId);
        try {
            String url = documentManagementServiceUrl + "/api/v1/requests/" + requestId + "/documents";
            DocumentResponse[] response = restTemplate.getForObject(url, DocumentResponse[].class);
            List<DocumentResponse> documents = response != null ? Arrays.asList(response) : List.of();
            log.debug("Found {} documents for request: {}", documents.size(), requestId);
            return documents;
        } catch (Exception e) {
            log.error("Error listing documents for request: {}", requestId, e);
            throw new ExternalServiceException("Failed to list documents", e);
        }
    }

    /**
     * Validate that a document exists.
     *
     * @param documentId the document ID
     * @return true if document exists, false otherwise
     */
    public boolean validateDocumentExists(UUID documentId) {
        log.debug("Validating document exists: {}", documentId);
        try {
            String url = documentManagementServiceUrl + "/api/v1/documents/" + documentId + "/exists";
            Boolean response = restTemplate.getForObject(url, Boolean.class);
            boolean exists = response != null && response;
            log.debug("Document existence check for {}: {}", documentId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validating document: {}", documentId, e);
            throw new ExternalServiceException("Failed to validate document", e);
        }
    }

    /**
     * DTO for document response from Document Management Service.
     */
    @Data
    public static class DocumentResponse {
        private UUID documentId;
        private String fileName;
        private Long fileSize;
        private String contentType;
        private String uploadedBy;
        private String uploadedAt;
    }

    /**
     * Exception thrown when external service call fails.
     */
    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String message) {
            super(message);
        }

        public ExternalServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
