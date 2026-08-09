package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationConnectionStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.repository.IntegrationStatusRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceImplTest {

    @Mock
    IntegrationStatusRepository integrationStatusRepository;

    @Mock
    NotionOAuthClient notionOAuthClient;

    @InjectMocks
    IntegrationServiceImpl integrationService;

    @Test
    void Notion_연동_시작시_인가_URL을_반환한다() {
        when(notionOAuthClient.buildAuthorizeUrl(1L)).thenReturn("https://api.notion.com/v1/oauth/authorize?...");

        NotionAuthorizeResponse response = integrationService.startNotionConnection(1L);

        assertThat(response.getAuthorizeUrl()).startsWith("https://api.notion.com");
    }

    @Test
    void Notion_콜백_처리시_기존_행이_없으면_새로_생성한다() {
        ReflectionTestUtils.setField(integrationService, "frontendUrl", "http://localhost:5173");
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("token", "workspace-1", "My Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(1L, IntegrationType.NOTION))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String redirectUrl = integrationService.handleNotionCallback("code123", "1");

        assertThat(redirectUrl).isEqualTo("http://localhost:5173/projects/1/integrations?connected=notion");
    }

    @Test
    void Notion_콜백_처리시_기존_행이_있으면_갱신한다() {
        ReflectionTestUtils.setField(integrationService, "frontendUrl", "http://localhost:5173");
        IntegrationStatus existing = IntegrationStatus.connectNotion(1L, "old-token", "old-ws", "Old Workspace");
        when(notionOAuthClient.exchangeCodeForToken("code123"))
                .thenReturn(new NotionTokenResponse("new-token", "new-ws", "New Workspace"));
        when(integrationStatusRepository.findByProjectIdAndType(1L, IntegrationType.NOTION))
                .thenReturn(Optional.of(existing));
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.handleNotionCallback("code123", "1");

        assertThat(existing.getAccessToken()).isEqualTo("new-token");
        assertThat(existing.getWorkspaceName()).isEqualTo("New Workspace");
    }

    @Test
    void state가_숫자가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> integrationService.handleNotionCallback("code123", "not-a-number"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_OAUTH_STATE));
    }

    @Test
    void GoogleMeet_연동시_기존_행이_없으면_새로_생성한다() {
        when(integrationStatusRepository.findByProjectIdAndType(1L, IntegrationType.GOOGLE_MEET))
                .thenReturn(Optional.empty());
        when(integrationStatusRepository.save(any(IntegrationStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IntegrationStatusResponse response = integrationService.connectGoogleMeet(1L);

        assertThat(response.getType()).isEqualTo(IntegrationType.GOOGLE_MEET);
        assertThat(response.getStatus()).isEqualTo(IntegrationConnectionStatus.CONNECTED);
    }

    @Test
    void 프로젝트의_연동_상태_목록을_조회한다() {
        IntegrationStatus notion = IntegrationStatus.connectNotion(1L, "token", "ws", "Workspace");
        when(integrationStatusRepository.findAllByProjectId(1L)).thenReturn(List.of(notion));

        List<IntegrationStatusResponse> responses = integrationService.getStatus(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getType()).isEqualTo(IntegrationType.NOTION);
    }
}
