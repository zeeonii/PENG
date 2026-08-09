package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.repository.IntegrationStatusRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class IntegrationServiceImpl implements IntegrationService {

    private final IntegrationStatusRepository integrationStatusRepository;
    private final NotionOAuthClient notionOAuthClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public NotionAuthorizeResponse startNotionConnection(Long projectId) {
        return NotionAuthorizeResponse.of(notionOAuthClient.buildAuthorizeUrl(projectId));
    }

    @Override
    @Transactional
    public String handleNotionCallback(String code, String state) {
        Long projectId = parseProjectId(state);
        NotionTokenResponse token = notionOAuthClient.exchangeCodeForToken(code);

        IntegrationStatus integrationStatus = integrationStatusRepository
                .findByProjectIdAndType(projectId, IntegrationType.NOTION)
                .orElseGet(() -> IntegrationStatus.connectNotion(
                        projectId, token.accessToken(), token.workspaceId(), token.workspaceName()));
        integrationStatus.updateNotionConnection(token.accessToken(), token.workspaceId(), token.workspaceName());
        integrationStatusRepository.save(integrationStatus);

        return frontendUrl + "/projects/" + projectId + "/integrations?connected=notion";
    }

    @Override
    @Transactional
    public IntegrationStatusResponse connectGoogleMeet(Long projectId) {
        IntegrationStatus integrationStatus = integrationStatusRepository
                .findByProjectIdAndType(projectId, IntegrationType.GOOGLE_MEET)
                .orElseGet(() -> IntegrationStatus.connectGoogleMeet(projectId));
        integrationStatus.updateGoogleMeetConnection();

        return IntegrationStatusResponse.from(integrationStatusRepository.save(integrationStatus));
    }

    @Override
    public List<IntegrationStatusResponse> getStatus(Long projectId) {
        return integrationStatusRepository.findAllByProjectId(projectId).stream()
                .map(IntegrationStatusResponse::from)
                .toList();
    }

    private Long parseProjectId(String state) {
        try {
            return Long.valueOf(state);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }
    }
}
