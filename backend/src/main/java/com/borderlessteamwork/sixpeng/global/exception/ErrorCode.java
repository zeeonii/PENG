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
    AI_TEAMMATE_NOT_INITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR, "M002", "AI teammate account is not initialized"),
    INVALID_TRANSLATION_DISPLAY_SETTING(HttpStatus.BAD_REQUEST, "M003",
            "At least one of original/translated text must be shown"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Project not found"),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "Not a participant of the project"),
    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "Project member not found"),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "P004", "Already a member of the project"),
    PROJECT_OWNER_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "P005", "Project owner cannot be removed"),
    PROJECT_AI_TEAMMATE_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "P006", "AI teammate cannot be removed"),
    PROJECT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "P007", "Only the project owner can delete the project"),
    PROJECT_AI_TEAMMATE_ROLE_CANNOT_BE_CHANGED(HttpStatus.BAD_REQUEST, "P008", "AI teammate's role cannot be changed"),
    TRANSLATION_FAILED(HttpStatus.BAD_GATEWAY, "T001", "Translation API call failed"),
    NOTION_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "I001", "Notion OAuth authorization failed"),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "I002", "Invalid OAuth state parameter"),
    NOTION_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "I003", "Notion document sync failed"),
    GOOGLE_MEET_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "I004", "Google Meet OAuth authorization failed"),
    GOOGLE_MEET_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "I005", "Google Meet document sync failed"),
    BRIEFING_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "해당 브리핑을 찾을 수 없습니다."),
    QNA_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "QnA history not found"),
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
