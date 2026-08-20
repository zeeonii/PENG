package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.AuthorizeUrlResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationConnectionStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.repository.IntegrationStatusRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceImplTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long MEMBER_ID = 10L;

    @Mock
    IntegrationStatusRepository integrationStatusRepository;

    @Mock
    DocumentRepository documentRepository;

    @Mock
    ProjectMemberRepository projectMemberRepository;

    @Mock
    NotionOAuthClient notionOAuthClient;

    @Mock
    NotionContentClient notionContentClient;

    @Mock
    GoogleMeetOAuthClient googleMeetOAuthClient;

    @Mock
    GoogleMeetContentClient googleMeetContentClient;

    @InjectMocks
    IntegrationServiceImpl integrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(integrationService, "frontendUrl", "http://localhost:5173");
    }

    private void asParticipant() {
        when(projectMemberRepository.existsByProjectIdAndMemberId(PROJECT_ID, MEMBER_ID)).thenReturn(true);
    }

    @Test
    void Notion_연동_시작시_인가_URL을_반환한다() {
        asParticipant();
        when(notionOAuthClient.buildAuthorizeUrl(PROJECT_ID)).thenReturn("https://api.notion.com/v1/oauth/authorize?...");

        AuthorizeUrlResponse response = integrationService.startNotionConnection(PROJECT_ID, MEMBER_ID);

        assertThat(response.getAuthorizeUrl()).startsWith("https://api.notion.com");
    }

    @Test
    void 프로젝트_참여자가_아니면_Notion_연동을_시작할_수_없다() {
        when(projectMemberRepository.existsByProjectIdAndMemberId(PROJECT_ID, MEMBER_ID)).thenReturn(false);

        assertThatThrownBy(() -> integrationService.startNotionConnection(PROJECT_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    @Test
    void Notion_콜백_처리시_기존_행이_없으면_새로_생성한다() {
        asParticipant();
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "workspace-1", "My Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String redirectUrl = integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        assertThat(redirectUrl).isEqualTo("http://localhost:5173/projects/1?connected=notion");
    }

    @Test
    void Notion_콜백_처리시_기존_행이_있으면_갱신한다() {
        asParticipant();
        IntegrationStatus existing = IntegrationStatus.connectNotion(PROJECT_ID, "old-token", "old-ws", "Old Workspace");
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("new-token", "new-ws", "New Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.of(existing));
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        assertThat(existing.getAccessToken()).isEqualTo("new-token");
        assertThat(existing.getWorkspaceName()).isEqualTo("New Workspace");
    }

    @Test
    void Notion_콜백_처리시_접근_가능한_페이지를_document로_수집한다() {
        asParticipant();
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "ws", "Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notionContentClient.searchAccessiblePages("token"))
                .thenReturn(List.of(new NotionPage("page-1", "회의록", "https://notion.so/page-1", "")));
        when(notionContentClient.fetchPageContent("token", "page-1")).thenReturn("오늘 논의한 내용");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(PROJECT_ID, DocumentSourceType.NOTION, "page-1"))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(1)).save(captor.capture());
        Document saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("회의록");
        assertThat(saved.getContent()).isEqualTo("오늘 논의한 내용");
        assertThat(saved.getSourceType()).isEqualTo(DocumentSourceType.NOTION);
    }

    @Test
    void Notion_콜백_처리시_이미_수집한_페이지는_내용만_갱신한다() {
        asParticipant();
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "ws", "Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notionContentClient.searchAccessiblePages("token"))
                .thenReturn(List.of(new NotionPage("page-1", "회의록", "https://notion.so/page-1", "")));
        when(notionContentClient.fetchPageContent("token", "page-1")).thenReturn("업데이트된 내용");
        Document existing = Document.collect(PROJECT_ID, DocumentSourceType.NOTION, "page-1", "회의록", "옛날 내용", "https://notion.so/page-1");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(PROJECT_ID, DocumentSourceType.NOTION, "page-1"))
                .thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(existing.getContent()).isEqualTo("업데이트된 내용");
    }

    @Test
    void Notion_콜백_처리시_데이터베이스_행의_속성값도_본문에_합쳐진다() {
        asParticipant();
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "ws", "Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // 표(데이터베이스)의 한 행: 본문 block은 비어 있고, 실제 내용은 속성(컬럼)에 들어있다.
        when(notionContentClient.searchAccessiblePages("token"))
                .thenReturn(List.of(new NotionPage("row-1", "홍길동", "https://notion.so/row-1", "소개: 백엔드 담당입니다\n")));
        when(notionContentClient.fetchPageContent("token", "row-1")).thenReturn("");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(PROJECT_ID, DocumentSourceType.NOTION, "row-1"))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("소개: 백엔드 담당입니다\n");
    }

    @Test
    void Notion_콜백_처리시_내용이_그대로면_문서를_다시_저장하지_않는다() {
        asParticipant();
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "ws", "Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notionContentClient.searchAccessiblePages("token"))
                .thenReturn(List.of(new NotionPage("page-1", "회의록", "https://notion.so/page-1", "")));
        when(notionContentClient.fetchPageContent("token", "page-1")).thenReturn("그대로인 내용");
        Document existing = Document.collect(PROJECT_ID, DocumentSourceType.NOTION, "page-1", "회의록", "그대로인 내용", "https://notion.so/page-1");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(PROJECT_ID, DocumentSourceType.NOTION, "page-1"))
                .thenReturn(Optional.of(existing));

        integrationService.handleNotionCallback("code123", "p1", MEMBER_ID);

        // 폴링으로 매번 재수집하면서 내용이 안 바뀐 문서까지 저장하면 collectedAt이 계속
        // 앞당겨져 브리핑이 "새 문서가 생겼다"고 오판하게 되므로, 저장 자체를 건너뛴다.
        verify(documentRepository, never()).save(any());
    }

    @Test
    void 폴링으로_연결된_모든_Notion_프로젝트를_재동기화한다() {
        IntegrationStatus connection = IntegrationStatus.connectNotion(PROJECT_ID, "token", "ws", "Workspace");
        when(integrationStatusRepository.findAllByTypeAndStatus(IntegrationType.NOTION, IntegrationConnectionStatus.CONNECTED))
                .thenReturn(List.of(connection));
        when(notionContentClient.searchAccessiblePages("token"))
                .thenReturn(List.of(new NotionPage("page-1", "회의록", "https://notion.so/page-1", "")));
        when(notionContentClient.fetchPageContent("token", "page-1")).thenReturn("새 내용");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(PROJECT_ID, DocumentSourceType.NOTION, "page-1"))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.resyncAllNotionConnections();

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void 폴링_중_한_프로젝트가_실패해도_나머지_프로젝트는_계속_처리한다() {
        IntegrationStatus failing = IntegrationStatus.connectNotion(PROJECT_ID, "bad-token", "ws1", "WS1");
        IntegrationStatus ok = IntegrationStatus.connectNotion(2L, "good-token", "ws2", "WS2");
        when(integrationStatusRepository.findAllByTypeAndStatus(IntegrationType.NOTION, IntegrationConnectionStatus.CONNECTED))
                .thenReturn(List.of(failing, ok));
        when(notionContentClient.searchAccessiblePages("bad-token")).thenThrow(new RuntimeException("boom"));
        when(notionContentClient.searchAccessiblePages("good-token"))
                .thenReturn(List.of(new NotionPage("page-2", "제목", "https://notion.so/page-2", "")));
        when(notionContentClient.fetchPageContent("good-token", "page-2")).thenReturn("내용");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(2L, DocumentSourceType.NOTION, "page-2"))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.resyncAllNotionConnections();

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void state가_숫자가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> integrationService.handleNotionCallback("code123", "not-a-number", MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_OAUTH_STATE));
    }

    @Test
    void 프로젝트_참여자가_아니면_GoogleMeet_연동을_시작할_수_없다() {
        when(projectMemberRepository.existsByProjectIdAndMemberId(PROJECT_ID, MEMBER_ID)).thenReturn(false);

        assertThatThrownBy(() -> integrationService.startGoogleMeetConnection(PROJECT_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    @Test
    void GoogleMeet_연동_시작시_인가_URL을_반환한다() {
        asParticipant();
        when(googleMeetOAuthClient.buildAuthorizeUrl(PROJECT_ID))
                .thenReturn("https://accounts.google.com/o/oauth2/v2/auth?...");

        AuthorizeUrlResponse response = integrationService.startGoogleMeetConnection(PROJECT_ID, MEMBER_ID);

        assertThat(response.getAuthorizeUrl()).startsWith("https://accounts.google.com");
    }

    @Test
    void GoogleMeet_콜백_처리시_기존_행이_없으면_새로_생성한다() {
        asParticipant();
        when(googleMeetOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new GoogleTokenResponse("meet-token"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.GOOGLE_MEET))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String redirectUrl = integrationService.handleGoogleMeetCallback("code123", "p1", MEMBER_ID);

        assertThat(redirectUrl).isEqualTo("http://localhost:5173/projects/1?connected=google-meet");
    }

    @Test
    void GoogleMeet_콜백_처리시_회의록이_있으면_document로_수집한다() {
        asParticipant();
        when(googleMeetOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new GoogleTokenResponse("meet-token"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.GOOGLE_MEET))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(googleMeetContentClient.fetchRecentConferenceRecords("meet-token"))
                .thenReturn(List.of(new GoogleConferenceRecord("conferenceRecords/abc", "2026-08-11T09:00:00Z")));
        when(googleMeetContentClient.fetchTranscriptContent("meet-token", "conferenceRecords/abc"))
                .thenReturn("A: 안녕하세요\nB: 네 안녕하세요\n");
        when(documentRepository.findByProjectIdAndSourceTypeAndSourceId(
                PROJECT_ID, DocumentSourceType.GOOGLE_MEET, "conferenceRecords/abc"))
                .thenReturn(Optional.empty());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleGoogleMeetCallback("code123", "p1", MEMBER_ID);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, times(1)).save(captor.capture());
        Document saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("A: 안녕하세요\nB: 네 안녕하세요\n");
        assertThat(saved.getSourceType()).isEqualTo(DocumentSourceType.GOOGLE_MEET);
    }

    @Test
    void GoogleMeet_콜백_처리시_녹취록이_없는_회의는_document로_수집하지_않는다() {
        asParticipant();
        when(googleMeetOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new GoogleTokenResponse("meet-token"));
        when(integrationStatusRepository.findByProjectIdAndType(PROJECT_ID, IntegrationType.GOOGLE_MEET))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(googleMeetContentClient.fetchRecentConferenceRecords("meet-token"))
                .thenReturn(List.of(new GoogleConferenceRecord("conferenceRecords/abc", "2026-08-11T09:00:00Z")));
        when(googleMeetContentClient.fetchTranscriptContent("meet-token", "conferenceRecords/abc"))
                .thenReturn("");

        integrationService.handleGoogleMeetCallback("code123", "p1", MEMBER_ID);

        verify(documentRepository, never()).save(any());
    }

    @Test
    void 프로젝트_참여자가_아니면_연동_상태를_조회할_수_없다() {
        when(projectMemberRepository.existsByProjectIdAndMemberId(PROJECT_ID, MEMBER_ID)).thenReturn(false);

        assertThatThrownBy(() -> integrationService.getStatus(PROJECT_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    @Test
    void 프로젝트의_연동_상태_목록을_조회한다() {
        asParticipant();
        IntegrationStatus notion = IntegrationStatus.connectNotion(PROJECT_ID, "token", "ws", "Workspace");
        when(integrationStatusRepository.findAllByProjectId(PROJECT_ID)).thenReturn(List.of(notion));

        List<IntegrationStatusResponse> responses = integrationService.getStatus(PROJECT_ID, MEMBER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getType()).isEqualTo(IntegrationType.NOTION);
    }
}
