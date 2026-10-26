package com.sews.reporting.integration;

/**
 * Exception levée lors d'erreurs avec les sources de données externes
 */
public class ExternalDataSourceException extends Exception {
    
    public ExternalDataSourceException(String message) {
        super(message);
    }
    
    public ExternalDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
