package com.golovko.backend.exception;

import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class WrongTargetObjectTypeException extends RuntimeException {

    public WrongTargetObjectTypeException(ActionType actionName, TargetObjectType objectType) {
        super(String.format("It's not allowed to %s for %s entity type!", actionName, objectType));
    }
}
