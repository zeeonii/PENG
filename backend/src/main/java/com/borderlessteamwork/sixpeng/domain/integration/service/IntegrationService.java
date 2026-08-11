package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;

import java.util.List;

public interface IntegrationService {

    NotionAuthorizeResponse startNotionConnection(Long projectId, Long memberId);

    String handleNotionCallback(String code, String state, Long memberId);

    IntegrationStatusResponse connectGoogleMeet(Long projectId, Long memberId);

    List<IntegrationStatusResponse> getStatus(Long projectId, Long memberId);
}
