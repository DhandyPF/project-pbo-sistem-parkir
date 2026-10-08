package com.parkir.exception;

public class ParkirException extends RuntimeException {
    public ParkirException(String message) {
        super(message);
    }

    public ParkirException(String message, Throwable cause) {
        super(message, cause);
    }
}
