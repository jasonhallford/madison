package io.miscellanea.madison.document;

public class DocumentException extends RuntimeException {
    public DocumentException(String message){
        super(message);
    }

    public DocumentException(String message, Throwable cause){
        super(message,cause);
    }
}
