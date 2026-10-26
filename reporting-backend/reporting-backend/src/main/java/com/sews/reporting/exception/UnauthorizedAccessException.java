package com.sews.reporting.exception;

/**
 * Exception levée quand un utilisateur tente d'accéder à une ressource sans autorisation
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String resourceName, Long resourceId) {
        super(String.format("Accès non autorisé à %s avec l'ID %d", resourceName, resourceId));
    }
}
