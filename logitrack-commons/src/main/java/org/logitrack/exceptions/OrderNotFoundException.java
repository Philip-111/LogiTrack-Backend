package org.logitrack.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class OrderNotFoundException extends RuntimeException {

    private String message;
    private HttpStatus httpStatus;

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public OrderNotFoundException() {

    }
}
