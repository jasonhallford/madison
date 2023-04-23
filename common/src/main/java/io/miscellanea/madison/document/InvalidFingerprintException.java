package io.miscellanea.madison.document;

public class InvalidFingerprintException extends IllegalArgumentException {
    public InvalidFingerprintException(String message) {
        super(message);
    }

    public InvalidFingerprintException(String message, Throwable cause) {
        super(message, cause);
    }
}
