package com.parkir.exception;

public class DataRusakException extends ParkirException {
    public DataRusakException(String message) {
        super(message);
    }

    public DataRusakException(String message, Throwable cause) {
        super(message, cause);
    }
}
