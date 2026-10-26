package com.sews.reporting.exception;

/**
 * Exception levée quand une ressource demandée n'est pas trouvée
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s avec l'ID %d n'a pas été trouvé", resourceName, id));
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s avec %s='%s' n'a pas été trouvé", resourceName, fieldName, fieldValue));
    }
}
