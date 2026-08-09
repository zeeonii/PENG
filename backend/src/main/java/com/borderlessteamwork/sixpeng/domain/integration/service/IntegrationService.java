package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;

import java.util.List;

public interface IntegrationService {

    NotionAuthorizeResponse startNotionConnection(Long projectId);

    String handleNotionCallback(String code, String state);

    IntegrationStatusResponse connectGoogleMeet(Long projectId);

    List<IntegrationStatusResponse> getStatus(Long projectId);
}
