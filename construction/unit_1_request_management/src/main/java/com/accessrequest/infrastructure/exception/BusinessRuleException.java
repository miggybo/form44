package com.accessrequest.infrastructure.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessRuleException extends ApplicationException {

    public BusinessRuleException(String message) {
        super("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super("BUSINESS_RULE_VIOLATION", message, cause);
    }
}
