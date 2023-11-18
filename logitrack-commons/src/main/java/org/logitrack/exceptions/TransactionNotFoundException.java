package org.logitrack.exceptions;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends RuntimeException{
    private String message;
    private HttpStatus httpStatus;

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public TransactionNotFoundException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
