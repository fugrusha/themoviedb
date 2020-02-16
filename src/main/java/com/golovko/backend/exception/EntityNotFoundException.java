package com.golovko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class entityClass, UUID id){
        super(String.format("Entity %s with id=%s is not found!", entityClass.getSimpleName(), id));
    }

    public EntityNotFoundException(Class entityClass, UUID id, Class parentEntityClass, UUID parentId){
        super(String.format("Entity %s with id=%s is not found in parent entity %s with id=%s!",
                entityClass.getSimpleName(), id, parentEntityClass.getSimpleName(), parentId));
    }
}
