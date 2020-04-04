package com.golovko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EntityWrongStatusException extends RuntimeException {

    public EntityWrongStatusException(Class entityClass, UUID id) {
        super(String.format("Entity %s with id=%s has not allowed status to update it!",
                entityClass.getSimpleName(), id));
    }

    public EntityWrongStatusException(String message) {
        super(message);
    }
}
