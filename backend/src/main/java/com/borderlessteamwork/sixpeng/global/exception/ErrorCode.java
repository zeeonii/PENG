package com.borderlessteamwork.sixpeng.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "Entity not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "Method not allowed"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "Access is denied"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C006", "Unauthorized"),

    TRANSLATION_FAILED(HttpStatus.BAD_GATEWAY, "T001", "Translation API call failed"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
