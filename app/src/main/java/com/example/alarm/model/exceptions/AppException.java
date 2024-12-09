package com.example.alarm.model.exceptions;

public class AppException extends RuntimeException {
    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(cause);
    }
}
