package io.miscellanea.madison.task;

public class TaskException extends RuntimeException {
    // Constructors
    public TaskException(String message){
        super(message);
    }

    public TaskException(String message, Throwable cause){
        super(message,cause);
    }
}
