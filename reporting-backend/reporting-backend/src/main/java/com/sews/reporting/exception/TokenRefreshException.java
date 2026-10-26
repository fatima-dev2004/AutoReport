package com.sews.reporting.exception;

/**
 * Exception levée lors d'erreurs de refresh token
 */
public class TokenRefreshException extends RuntimeException {
    
    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
