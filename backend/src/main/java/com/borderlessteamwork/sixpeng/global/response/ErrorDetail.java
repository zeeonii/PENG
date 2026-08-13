package com.borderlessteamwork.sixpeng.global.response;

import lombok.Getter;

@Getter
public class ErrorDetail {

    private final String field;
    private final String value;
    private final String reason;

    public ErrorDetail(String field, String value, String reason) {
        this.field = field;
        this.value = value;
        this.reason = reason;
    }
}
