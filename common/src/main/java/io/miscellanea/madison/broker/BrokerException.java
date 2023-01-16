package io.miscellanea.madison.broker;

public class BrokerException extends RuntimeException {
    // Constructors
    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(String message, Throwable cause) {
        super(message, cause);
    }
}
