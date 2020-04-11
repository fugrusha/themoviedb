package com.golovko.backend.exception;

import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ActionOfUserDuplicatedException extends RuntimeException {

    public ActionOfUserDuplicatedException(UUID userId, ActionType actionType,
                                   TargetObjectType targetType, UUID targetId) {
        super(String.format("User with id=%s has already %s for %s with id=%s",
                userId, actionType, targetType, targetId));
    }
}
