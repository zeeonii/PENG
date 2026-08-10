package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository;
    private final NotionOAuthClient notionOAuthClient;
    private final NotionContentClient notionContentClient;

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

        syncNotionDocuments(projectId, token.accessToken());

        return frontendUrl + "/projects/" + projectId + "/integrations?connected=notion";
    }

    /** 연동 시점의 Notion page들을 document로 수집한다. 이후 재동기화(주기적 갱신)는 후속 작업. */
    private void syncNotionDocuments(Long projectId, String accessToken) {
        for (NotionPage page : notionContentClient.searchAccessiblePages(accessToken)) {
            String content = notionContentClient.fetchPageContent(accessToken, page.id());

            Document document = documentRepository
                    .findByProjectIdAndSourceTypeAndSourceId(projectId, DocumentSourceType.NOTION, page.id())
                    .orElseGet(() -> Document.collect(
                            projectId, DocumentSourceType.NOTION, page.id(), page.title(), content, page.url()));
            document.updateContent(page.title(), content, page.url());
            documentRepository.save(document);
        }
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
