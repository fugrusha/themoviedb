package com.golovko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ControllerValidationException extends RuntimeException {

    public ControllerValidationException(String message) {
        super(message);
    }
}
