package org.logitrack.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Setter
@Getter
public class UserExistException extends RuntimeException{

    private String message;
    private HttpStatus httpStatus;
    public UserExistException(String message) {
        super(message);

    }
    public UserExistException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
