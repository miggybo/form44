package com.accessrequest.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for health check endpoints.
 * Provides liveness and readiness probes for Kubernetes and monitoring systems.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Health check and monitoring endpoints")
public class HealthCheckController {

    private final HealthEndpoint healthEndpoint;

    /**
     * Liveness probe - indicates if the service is running.
     * Used by Kubernetes to determine if the pod should be restarted.
     *
     * @return 200 OK if service is alive
     */
    @GetMapping("/live")
    @Operation(summary = "Liveness probe", description = "Indicates if the service is running (Kubernetes liveness probe)")
    @ApiResponse(responseCode = "200", description = "Service is alive")
    public ResponseEntity<Map<String, String>> liveness() {
        log.debug("Liveness probe called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is alive");
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe - indicates if the service is ready to accept traffic.
     * Used by Kubernetes to determine if the pod should receive traffic.
     *
     * @return 200 OK if service is ready, 503 if not ready
     */
    @GetMapping("/ready")
    @Operation(summary = "Readiness probe", description = "Indicates if the service is ready to accept traffic (Kubernetes readiness probe)")
    @ApiResponse(responseCode = "200", description = "Service is ready")
    @ApiResponse(responseCode = "503", description = "Service is not ready")
    public ResponseEntity<Map<String, String>> readiness() {
        log.debug("Readiness probe called");

        try {
            HealthComponent health = healthEndpoint.health();
            String status = health.getStatus().toString();

            Map<String, String> response = new HashMap<>();
            response.put("status", status);
            response.put("message", "Service is ready to accept traffic");

            if ("UP".equals(status)) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            log.error("Error checking readiness", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("message", "Service is not ready: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Detailed health information.
     * Returns comprehensive health status of all components.
     *
     * @return detailed health information
     */
    @GetMapping("/detailed")
    @Operation(summary = "Detailed health status", description = "Returns comprehensive health status of all service components")
    @ApiResponse(responseCode = "200", description = "Health information retrieved")
    public ResponseEntity<HealthComponent> getDetailedHealth() {
        log.debug("Detailed health check called");
        HealthComponent health = healthEndpoint.health();
        return ResponseEntity.ok(health);
    }
}
