package com.example.expressioneval.exception.exceptions;

public class ParseException extends RuntimeException {
    private final int position;

    public ParseException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}