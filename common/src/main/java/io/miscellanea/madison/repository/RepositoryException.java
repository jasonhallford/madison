package io.miscellanea.madison.repository;

public class RepositoryException extends RuntimeException {
    // Constructors
    public RepositoryException(String message){
        super(message);
    }

    public RepositoryException(String message, Throwable cause){
        super(message,cause);
    }
}
