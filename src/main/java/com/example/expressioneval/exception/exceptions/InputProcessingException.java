package com.example.expressioneval.exception.exceptions;


public class InputProcessingException extends RuntimeException {
    public InputProcessingException(String message) {
        super(message);
    }

    public InputProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}