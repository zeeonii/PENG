package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.AuthorizeUrlResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationConnectionStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.repository.IntegrationStatusRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import com.borderlessteamwork.sixpeng.domain.qna.event.DocumentSavedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class IntegrationServiceImpl implements IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationServiceImpl.class);
    private final ApplicationEventPublisher eventPublisher;

    private final IntegrationStatusRepository integrationStatusRepository;
    private final DocumentRepository documentRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotionOAuthClient notionOAuthClient;
    private final NotionContentClient notionContentClient;
    private final GoogleMeetOAuthClient googleMeetOAuthClient;
    private final GoogleMeetContentClient googleMeetContentClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public AuthorizeUrlResponse startNotionConnection(Long projectId, Long memberId) {
        validateParticipant(projectId, memberId);
        return AuthorizeUrlResponse.of(notionOAuthClient.buildAuthorizeUrl(projectId));
    }

    @Override
    @Transactional
    public String handleNotionCallback(String code, String state, Long memberId) {
        Long projectId = parseProjectId(state);
        validateParticipant(projectId, memberId);
        NotionTokenResponse token = notionOAuthClient.exchangeCodeForToken(code);

        IntegrationStatus integrationStatus = integrationStatusRepository
                .findByProjectIdAndType(projectId, IntegrationType.NOTION)
                .orElseGet(() -> IntegrationStatus.connectNotion(
                        projectId, token.accessToken(), token.workspaceId(), token.workspaceName()));
        integrationStatus.updateNotionConnection(token.accessToken(), token.workspaceId(), token.workspaceName());
        integrationStatusRepository.save(integrationStatus);

        // 문서 수집이 실패해도 연동(토큰 저장) 자체는 유지되어야 한다.
        try {
            syncNotionDocuments(projectId, token.accessToken());
        } catch (Exception e) {
            log.warn("Notion 문서 수집 실패 (연동 상태는 유지됨): projectId={}", projectId, e);
        }

        // 프론트 라우터에 /projects/{id}/integrations 경로가 없어(전부 catch-all -> /login),
        // 실제로 존재하는 프로젝트 상세 경로로 보낸다.
        return frontendUrl + "/projects/" + projectId + "?connected=notion";
    }

    @Override
    @Transactional
    public void resyncAllNotionConnections() {
        List<IntegrationStatus> connections = integrationStatusRepository
                .findAllByTypeAndStatus(IntegrationType.NOTION, IntegrationConnectionStatus.CONNECTED);

        for (IntegrationStatus connection : connections) {
            try {
                syncNotionDocuments(connection.getProjectId(), connection.getAccessToken());
                connection.markSynced();
            } catch (Exception e) {
                log.warn("Notion 주기적 재동기화 실패: projectId={}", connection.getProjectId(), e);
            }
        }
    }

    /**
     * Notion page들을 document로 수집한다. 연동 시점과 주기적 재동기화(폴링) 양쪽에서 호출한다.
     *
     * <p>폴링은 변경 여부와 무관하게 매번 모든 page를 다시 읽어오므로, 내용이 그대로인
     * page까지 매번 갱신 처리하면 (1) collectedAt이 계속 앞당겨져 브리핑이 "새 문서가
     * 계속 생긴 것"처럼 오판하고, (2) 임베딩 이벤트가 매번 다시 발행되어 OpenAI 호출이
     * 낭비된다. 그래서 실제로 title/content/url이 달라졌을 때만 갱신한다.
     */
    private void syncNotionDocuments(Long projectId, String accessToken) {
        for (NotionPage page : notionContentClient.searchAccessiblePages(accessToken)) {
            String content = notionContentClient.fetchPageContent(accessToken, page.id());

            Optional<Document> existing = documentRepository
                    .findByProjectIdAndSourceTypeAndSourceId(projectId, DocumentSourceType.NOTION, page.id());

            if (existing.isPresent() && isUnchanged(existing.get(), page.title(), content, page.url())) {
                continue;
            }

            Document document = existing.orElseGet(() -> Document.collect(
                    projectId, DocumentSourceType.NOTION, page.id(), page.title(), content, page.url()));
            document.updateContent(page.title(), content, page.url());
            documentRepository.save(document);
            eventPublisher.publishEvent(new DocumentSavedEvent(document.getId()));
        }
    }

    private boolean isUnchanged(Document document, String title, String content, String url) {
        return Objects.equals(document.getTitle(), title)
                && Objects.equals(document.getContent(), content)
                && Objects.equals(document.getSourceUrl(), url);
    }

    @Override
    public AuthorizeUrlResponse startGoogleMeetConnection(Long projectId, Long memberId) {
        validateParticipant(projectId, memberId);
        return AuthorizeUrlResponse.of(googleMeetOAuthClient.buildAuthorizeUrl(projectId));
    }

    @Override
    @Transactional
    public String handleGoogleMeetCallback(String code, String state, Long memberId) {
        Long projectId = parseProjectId(state);
        validateParticipant(projectId, memberId);
        GoogleTokenResponse token = googleMeetOAuthClient.exchangeCodeForToken(code);

        IntegrationStatus integrationStatus = integrationStatusRepository
                .findByProjectIdAndType(projectId, IntegrationType.GOOGLE_MEET)
                .orElseGet(() -> IntegrationStatus.connectGoogleMeet(projectId, token.accessToken()));
        integrationStatus.updateGoogleMeetConnection(token.accessToken());
        integrationStatusRepository.save(integrationStatus);

        // 문서 수집이 실패해도 연동(토큰 저장) 자체는 유지되어야 한다.
        try {
            syncGoogleMeetDocuments(projectId, token.accessToken());
        } catch (Exception e) {
            log.warn("Google Meet 문서 수집 실패 (연동 상태는 유지됨): projectId={}", projectId, e);
        }

        return frontendUrl + "/projects/" + projectId + "?connected=google-meet";
    }

    /**
     * 연동 시점 기준 최근 회의 기록의 녹취록을 document로 수집한다.
     * 녹화/받아쓰기가 켜져 있던 회의가 없으면(Workspace 설정, 요금제에 따라) 아무것도 수집되지 않는다.
     */
    private void syncGoogleMeetDocuments(Long projectId, String accessToken) {
        for (GoogleConferenceRecord record : googleMeetContentClient.fetchRecentConferenceRecords(accessToken)) {
            String content = googleMeetContentClient.fetchTranscriptContent(accessToken, record.name());
            if (content.isBlank()) {
                continue;
            }
            String title = "Google Meet 회의록 (" + record.startTime() + ")";

            Document document = documentRepository
                    .findByProjectIdAndSourceTypeAndSourceId(projectId, DocumentSourceType.GOOGLE_MEET, record.name())
                    .orElseGet(() -> Document.collect(
                            projectId, DocumentSourceType.GOOGLE_MEET, record.name(), title, content, null));
            document.updateContent(title, content, null);
            documentRepository.save(document);
            eventPublisher.publishEvent(new DocumentSavedEvent(document.getId()));
        }
    }

    @Override
    public List<IntegrationStatusResponse> getStatus(Long projectId, Long memberId) {
        validateParticipant(projectId, memberId);
        return integrationStatusRepository.findAllByProjectId(projectId).stream()
                .map(IntegrationStatusResponse::from)
                .toList();
    }

    /** state는 "p{projectId}" 형식으로 발급한다 (순수 숫자 문자열이면 거부하는 provider가 있어 접두사를 붙임). */
    private Long parseProjectId(String state) {
        if (state == null || !state.startsWith("p")) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }
        try {
            return Long.valueOf(state.substring(1));
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }
    }

    private void validateParticipant(Long projectId, Long memberId) {
        if (!projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
