package com.accessrequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot Application for Request Management Service.
 * 
 * This is the entry point for the Access Request Processing System - Unit 1.
 * The service manages the complete lifecycle of access requests with multi-level
 * approval workflow orchestration.
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class RequestManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestManagementServiceApplication.class, args);
    }

}
