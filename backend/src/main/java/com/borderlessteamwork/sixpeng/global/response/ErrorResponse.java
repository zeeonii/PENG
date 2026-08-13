package com.borderlessteamwork.sixpeng.global.response;

import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<ErrorDetail> errors;

    private ErrorResponse(ErrorCode errorCode, List<ErrorDetail> errors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode, Collections.emptyList());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<ErrorDetail> errors) {
        return new ErrorResponse(errorCode, errors);
    }
}
