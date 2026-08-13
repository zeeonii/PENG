package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record NotionTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("workspace_id") String workspaceId,
        @JsonProperty("workspace_name") String workspaceName
) {
}
