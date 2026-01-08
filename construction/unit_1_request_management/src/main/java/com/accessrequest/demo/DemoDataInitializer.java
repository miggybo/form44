package com.accessrequest.demo;

import com.accessrequest.application.dto.CreateAccessTypeRequest;
import com.accessrequest.application.dto.CreateRequestRequest;
import com.accessrequest.application.service.AccessTypeApplicationService;
import com.accessrequest.application.service.RequestApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Demo data initializer for local development and testing.
 * Populates the database with sample access types and requests.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final AccessTypeApplicationService accessTypeApplicationService;
    private final RequestApplicationService requestApplicationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing demo data...");

        try {
            initializeAccessTypes();
            initializeSampleRequests();
            log.info("Demo data initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing demo data", e);
        }
    }

    /**
     * Initialize sample access types.
     */
    private void initializeAccessTypes() {
        log.info("Creating sample access types...");

        // Operating System Access
        CreateAccessTypeRequest osAccessType = CreateAccessTypeRequest.builder()
                .name("OS")
                .description("Operating System access for servers and workstations")
                .administratorRoles(Arrays.asList("ADMIN", "SYSTEM_ADMIN"))
                .build();

        try {
            accessTypeApplicationService.createAccessType(osAccessType);
            log.info("Created OS access type");
        } catch (Exception e) {
            log.debug("OS access type already exists or error occurred: {}", e.getMessage());
        }

        // Web Application Access
        CreateAccessTypeRequest webAppAccessType = CreateAccessTypeRequest.builder()
                .name("WebApp")
                .description("Web application access for internal systems")
                .administratorRoles(Arrays.asList("ADMIN", "APP_ADMIN"))
                .build();

        try {
            accessTypeApplicationService.createAccessType(webAppAccessType);
            log.info("Created WebApp access type");
        } catch (Exception e) {
            log.debug("WebApp access type already exists or error occurred: {}", e.getMessage());
        }

        // Database Access
        CreateAccessTypeRequest dbAccessType = CreateAccessTypeRequest.builder()
                .name("Database")
                .description("Database access for development and production systems")
                .administratorRoles(Arrays.asList("ADMIN", "DBA"))
                .build();

        try {
            accessTypeApplicationService.createAccessType(dbAccessType);
            log.info("Created Database access type");
        } catch (Exception e) {
            log.debug("Database access type already exists or error occurred: {}", e.getMessage());
        }
    }

    /**
     * Initialize sample requests for demonstration.
     */
    private void initializeSampleRequests() {
        log.info("Creating sample requests...");

        UUID requestorId = UUID.randomUUID();

        // Sample Request 1: OS Access
        CreateRequestRequest osRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Production Linux Server")
                .justification("Need access to production Linux server for deployment and maintenance of microservices")
                .build();

        try {
            requestApplicationService.createRequest(osRequest);
            log.info("Created sample OS access request");
        } catch (Exception e) {
            log.debug("Error creating OS request: {}", e.getMessage());
        }

        // Sample Request 2: Web App Access
        CreateRequestRequest webAppRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("WebApp")
                .systemName("Internal Admin Portal")
                .justification("Need access to internal admin portal for user management and system configuration")
                .build();

        try {
            requestApplicationService.createRequest(webAppRequest);
            log.info("Created sample WebApp access request");
        } catch (Exception e) {
            log.debug("Error creating WebApp request: {}", e.getMessage());
        }

        // Sample Request 3: Database Access
        CreateRequestRequest dbRequest = CreateRequestRequest.builder()
                .requestorId(requestorId)
                .accessType("Database")
                .systemName("Production PostgreSQL Database")
                .justification("Need read-only access to production database for analytics and reporting purposes")
                .build();

        try {
            requestApplicationService.createRequest(dbRequest);
            log.info("Created sample Database access request");
        } catch (Exception e) {
            log.debug("Error creating Database request: {}", e.getMessage());
        }

        log.info("Sample requests created successfully");
    }
}
