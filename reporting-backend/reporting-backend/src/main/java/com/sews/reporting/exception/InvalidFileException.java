package com.sews.reporting.exception;

/**
 * Exception levée quand un fichier est invalide ou corrompu
 */
public class InvalidFileException extends RuntimeException {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
