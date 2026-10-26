package com.sews.reporting.exception;

/**
 * Exception levée lors d'erreurs avec le service email (SMTP)
 */
public class EmailServiceException extends RuntimeException {
    
    public EmailServiceException(String message) {
        super(message);
    }
    
    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
