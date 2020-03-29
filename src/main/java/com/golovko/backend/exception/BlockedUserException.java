package com.golovko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BlockedUserException extends RuntimeException {

    public BlockedUserException(UUID userId) {
        super(String.format("Permission denied. User with id: %s is blocked.", userId));
    }
}
