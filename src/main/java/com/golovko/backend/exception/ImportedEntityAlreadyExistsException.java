package com.golovko.backend.exception;

public class ImportedEntityAlreadyExistsException extends Exception {

    public ImportedEntityAlreadyExistsException(String message) {
        super(message);
    }
}
