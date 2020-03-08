package com.golovko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class entityClass, UUID id) {
        super(String.format("Entity %s with id=%s is not found!", entityClass.getSimpleName(), id));
    }

    public EntityNotFoundException(Class entityClass, UUID id, Class targetEntityClass, UUID targetId) {
        super(String.format("Entity %s with id=%s is not found for entity %s with id=%s!",
                entityClass.getSimpleName(), id, targetEntityClass.getSimpleName(), targetId));
    }
}
