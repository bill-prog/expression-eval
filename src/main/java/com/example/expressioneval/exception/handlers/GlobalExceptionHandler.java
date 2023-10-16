package com.example.expressioneval.exception.handlers;

import com.example.expressioneval.exception.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "An I/O error occurred: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            ExpressionNotFoundException.class,
            EvaluationException.class,
            ParseException.class,
            TokenizerException.class,
            InputProcessingException.class,
            SerializationException.class
    })
    public ResponseEntity<Object> handleCommonExceptions(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());

        if (ex instanceof ExpressionNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        else if (ex instanceof ParseException) {
            body.put("position", ((ParseException) ex).getPosition());
        }

        return new ResponseEntity<>(body, status);
    }
}
