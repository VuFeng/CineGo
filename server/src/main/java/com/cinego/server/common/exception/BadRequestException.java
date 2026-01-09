package com.cinego.server.common.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST");
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, "BAD_REQUEST", cause);
    }
}
