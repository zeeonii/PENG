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

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "Member not found"),

    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Project not found"),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "Not a participant of the project"),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "Project member not found"),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "P004", "Already a member of the project"),
    PROJECT_OWNER_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "P005", "Project owner cannot be removed"),

    TRANSLATION_FAILED(HttpStatus.BAD_GATEWAY, "T001", "Translation API call failed"),

    NOTION_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "I001", "Notion OAuth authorization failed"),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "I002", "Invalid OAuth state parameter"),
    NOTION_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "I003", "Notion document sync failed"),
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
