package com.accessrequest.infrastructure.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
