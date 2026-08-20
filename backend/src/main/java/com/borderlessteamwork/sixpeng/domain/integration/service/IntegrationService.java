package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.AuthorizeUrlResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;

import java.util.List;

public interface IntegrationService {

    AuthorizeUrlResponse startNotionConnection(Long projectId, Long memberId);

    String handleNotionCallback(String code, String state, Long memberId);

    AuthorizeUrlResponse startGoogleMeetConnection(Long projectId, Long memberId);

    String handleGoogleMeetCallback(String code, String state, Long memberId);

    List<IntegrationStatusResponse> getStatus(Long projectId, Long memberId);

    /** 연동된 모든 프로젝트의 Notion 페이지를 다시 읽어와 변경사항을 반영한다 (주기적 폴링용). */
    void resyncAllNotionConnections();
}
