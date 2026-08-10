package com.borderlessteamwork.sixpeng.domain.integration.controller;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.service.IntegrationService;
import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.support.TestLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntegrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    IntegrationService integrationService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.ofGoogle("g-integration", "integration@example.com", "연동러", Language.KR));
    }

    @Test
    @DisplayName("로그인하지 않으면 401이다")
    void 로그인하지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/projects/{projectId}/integrations/status", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Notion 연동 시작시 인가 URL을 반환한다")
    void Notion_연동_시작시_인가_URL을_반환한다() throws Exception {
        when(integrationService.startNotionConnection(1L))
                .thenReturn(NotionAuthorizeResponse.of("https://api.notion.com/v1/oauth/authorize?client_id=x"));

        mockMvc.perform(post("/projects/{projectId}/integrations/notion", 1L).with(TestLogin.as(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorizeUrl").value("https://api.notion.com/v1/oauth/authorize?client_id=x"));
    }

    @Test
    @DisplayName("CSRF 토큰이 없으면 로그인했어도 403이다")
    void CSRF_토큰이_없으면_403을_반환한다() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/integrations/notion", 1L).with(TestLogin.withoutCsrf(member)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Notion 콜백은 프론트엔드로 리다이렉트한다")
    void Notion_콜백은_프론트엔드로_리다이렉트한다() throws Exception {
        when(integrationService.handleNotionCallback("code123", "1"))
                .thenReturn("http://localhost:5173/projects/1/integrations?connected=notion");

        mockMvc.perform(get("/integrations/notion/callback").with(TestLogin.as(member))
                        .param("code", "code123")
                        .param("state", "1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/projects/1/integrations?connected=notion"));
    }

    @Test
    @DisplayName("Google Meet 연동을 시작한다")
    void GoogleMeet_연동을_시작한다() throws Exception {
        IntegrationStatus status = IntegrationStatus.connectGoogleMeet(1L);
        when(integrationService.connectGoogleMeet(1L)).thenReturn(IntegrationStatusResponse.from(status));

        mockMvc.perform(post("/projects/{projectId}/integrations/google-meet", 1L).with(TestLogin.as(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GOOGLE_MEET"))
                .andExpect(jsonPath("$.status").value("CONNECTED"));
    }

    @Test
    @DisplayName("연동 상태 목록을 조회한다")
    void 연동_상태_목록을_조회한다() throws Exception {
        IntegrationStatus notion = IntegrationStatus.connectNotion(1L, "token", "ws", "Workspace");
        when(integrationService.getStatus(1L)).thenReturn(List.of(IntegrationStatusResponse.from(notion)));

        mockMvc.perform(get("/projects/{projectId}/integrations/status", 1L).with(TestLogin.as(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value(IntegrationType.NOTION.name()));
    }
}
