package com.example.alarm.model.exceptions;

public class BackendException extends AppException {
    private final int code;

    public BackendException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

