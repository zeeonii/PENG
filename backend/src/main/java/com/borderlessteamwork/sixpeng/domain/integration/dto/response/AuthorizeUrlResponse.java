package com.borderlessteamwork.sixpeng.domain.integration.dto.response;

import lombok.Getter;

@Getter
public class AuthorizeUrlResponse {

    private final String authorizeUrl;

    private AuthorizeUrlResponse(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public static AuthorizeUrlResponse of(String authorizeUrl) {
        return new AuthorizeUrlResponse(authorizeUrl);
    }
}
