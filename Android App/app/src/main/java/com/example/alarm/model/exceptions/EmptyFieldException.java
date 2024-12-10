package com.example.alarm.model.exceptions;

import java.lang.reflect.Field;

public class EmptyFieldException extends AppException {
    private final Field field;

    public EmptyFieldException(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}