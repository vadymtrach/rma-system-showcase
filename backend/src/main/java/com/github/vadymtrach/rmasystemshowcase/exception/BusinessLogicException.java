package com.github.vadymtrach.rmasystemshowcase.exception;

import org.springframework.http.HttpStatus;

public class BusinessLogicException extends ApiException {
    public BusinessLogicException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
