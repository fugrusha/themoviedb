package com.golovko.backend.exception;

import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class WrongTypeOfTargetObjectException extends RuntimeException {

    public WrongTypeOfTargetObjectException(ActionType actionName, TargetObjectType objectType) {
        super(String.format("Action %s is not supported for %s entity type!",
                actionName, objectType));
    }
}
