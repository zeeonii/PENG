package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record GoogleTokenResponse(
        @JsonProperty("access_token") String accessToken
) {
}
