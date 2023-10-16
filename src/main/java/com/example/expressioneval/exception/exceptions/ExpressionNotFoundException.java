package com.example.expressioneval.exception.exceptions;

public class ExpressionNotFoundException extends RuntimeException {
    public ExpressionNotFoundException(String message) {
        super(message);
    }
}
