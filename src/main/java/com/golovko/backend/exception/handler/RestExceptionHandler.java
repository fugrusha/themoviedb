package com.golovko.backend.exception.handler;

import com.golovko.backend.exception.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            ActionOfUserDuplicatedException.class,
            LinkDuplicatedException.class,
            ControllerValidationException.class,
            EntityWrongStatusException.class,
            Exception.class})
    public ResponseEntity<Object> handleException(Exception e) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        HttpStatus httpStatus = status != null ? status.code() : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorInfo errorInfo = new ErrorInfo(httpStatus, e.getClass(), e.getMessage());
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String errorMsg = "Invalid type of " + e.getName() + ". It should be of type " + e.getRequiredType().getName();

        ErrorInfo errorInfo = new ErrorInfo(httpStatus, e.getClass(), errorMsg);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @NotNull
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus httpStatus, WebRequest request
    ) {
        String errorMessage = ex.getBindingResult().getFieldErrors().toString();

        ErrorInfo errorInfo = new ErrorInfo(httpStatus, ex.getClass(), errorMessage);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }
}
