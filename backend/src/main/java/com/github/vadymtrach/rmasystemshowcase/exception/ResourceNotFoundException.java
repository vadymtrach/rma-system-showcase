package com.github.vadymtrach.rmasystemshowcase.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        this(resource + " not found with id: " + id);
    }
}
