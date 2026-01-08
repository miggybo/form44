package com.accessrequest.infrastructure.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for integrating with Administration Service.
 * Provides methods to retrieve user and role information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdministrationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${external.services.administration.url:http://localhost:8081}")
    private String administrationServiceUrl;

    /**
     * Get Head of Office for a requestor.
     *
     * @param requestorId the requestor ID
     * @return the Head of Office user ID
     */
    public UUID getHeadOfOfficeForRequestor(UUID requestorId) {
        log.debug("Getting Head of Office for requestor: {}", requestorId);
        try {
            String url = administrationServiceUrl + "/api/v1/users/" + requestorId + "/head-of-office";
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            if (response != null && response.getUserId() != null) {
                log.debug("Head of Office found: {}", response.getUserId());
                return response.getUserId();
            }
            log.warn("No Head of Office found for requestor: {}", requestorId);
            return null;
        } catch (Exception e) {
            log.error("Error getting Head of Office for requestor: {}", requestorId, e);
            throw new ExternalServiceException("Failed to get Head of Office", e);
        }
    }

    /**
     * Get SMD/RDC Reviewer for an access type.
     *
     * @param accessType the access type
     * @return the reviewer user ID
     */
    public UUID getReviewerForAccessType(String accessType) {
        log.debug("Getting reviewer for access type: {}", accessType);
        try {
            String url = administrationServiceUrl + "/api/v1/roles/reviewer?accessType=" + accessType;
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            if (response != null && response.getUserId() != null) {
                log.debug("Reviewer found: {}", response.getUserId());
                return response.getUserId();
            }
            log.warn("No reviewer found for access type: {}", accessType);
            return null;
        } catch (Exception e) {
            log.error("Error getting reviewer for access type: {}", accessType, e);
            throw new ExternalServiceException("Failed to get reviewer", e);
        }
    }

    /**
     * Get SMD/RDC Head.
     *
     * @return the SMD/RDC Head user ID
     */
    public UUID getSmdHeadId() {
        log.debug("Getting SMD/RDC Head");
        try {
            String url = administrationServiceUrl + "/api/v1/roles/smd-head";
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            if (response != null && response.getUserId() != null) {
                log.debug("SMD/RDC Head found: {}", response.getUserId());
                return response.getUserId();
            }
            log.warn("No SMD/RDC Head found");
            return null;
        } catch (Exception e) {
            log.error("Error getting SMD/RDC Head", e);
            throw new ExternalServiceException("Failed to get SMD/RDC Head", e);
        }
    }

    /**
     * Get Administrator for an access type.
     *
     * @param accessType the access type
     * @return the administrator user ID
     */
    public UUID getAdministratorForAccessType(String accessType) {
        log.debug("Getting administrator for access type: {}", accessType);
        try {
            String url = administrationServiceUrl + "/api/v1/roles/administrator?accessType=" + accessType;
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            if (response != null && response.getUserId() != null) {
                log.debug("Administrator found: {}", response.getUserId());
                return response.getUserId();
            }
            log.warn("No administrator found for access type: {}", accessType);
            return null;
        } catch (Exception e) {
            log.error("Error getting administrator for access type: {}", accessType, e);
            throw new ExternalServiceException("Failed to get administrator", e);
        }
    }

    /**
     * Get user by ID.
     *
     * @param userId the user ID
     * @return the user response
     */
    public UserResponse getUserById(UUID userId) {
        log.debug("Getting user by ID: {}", userId);
        try {
            String url = administrationServiceUrl + "/api/v1/users/" + userId;
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            log.debug("User found: {}", userId);
            return response;
        } catch (Exception e) {
            log.error("Error getting user: {}", userId, e);
            throw new ExternalServiceException("Failed to get user", e);
        }
    }

    /**
     * Check if user has a specific role.
     *
     * @param userId the user ID
     * @param role the role to check
     * @return true if user has role, false otherwise
     */
    public boolean hasRole(UUID userId, String role) {
        log.debug("Checking if user {} has role: {}", userId, role);
        try {
            String url = administrationServiceUrl + "/api/v1/users/" + userId + "/roles/" + role;
            Boolean response = restTemplate.getForObject(url, Boolean.class);
            log.debug("Role check result for user {}: {}", userId, response);
            return response != null && response;
        } catch (Exception e) {
            log.error("Error checking role for user: {}", userId, e);
            throw new ExternalServiceException("Failed to check user role", e);
        }
    }

    /**
     * DTO for user response from Administration Service.
     */
    @Data
    public static class UserResponse {
        private UUID userId;
        private String name;
        private String email;
        private String role;
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
