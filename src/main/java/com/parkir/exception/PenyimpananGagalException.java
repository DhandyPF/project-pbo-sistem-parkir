package com.parkir.exception;

public class PenyimpananGagalException extends ParkirException {
    public PenyimpananGagalException(String message) {
        super(message);
    }

    public PenyimpananGagalException(String message, Throwable cause) {
        super(message, cause);
    }
}
