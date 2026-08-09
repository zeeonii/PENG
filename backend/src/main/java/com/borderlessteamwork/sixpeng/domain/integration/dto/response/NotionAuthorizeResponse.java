package com.borderlessteamwork.sixpeng.domain.integration.dto.response;

import lombok.Getter;

@Getter
public class NotionAuthorizeResponse {

    private final String authorizeUrl;

    private NotionAuthorizeResponse(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public static NotionAuthorizeResponse of(String authorizeUrl) {
        return new NotionAuthorizeResponse(authorizeUrl);
    }
}
