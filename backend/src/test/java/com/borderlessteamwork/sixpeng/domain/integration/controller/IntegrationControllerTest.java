package com.borderlessteamwork.sixpeng.domain.integration.controller;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.NotionAuthorizeResponse;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import com.borderlessteamwork.sixpeng.domain.integration.service.IntegrationService;
import com.borderlessteamwork.sixpeng.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IntegrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class IntegrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    IntegrationService integrationService;

    @Test
    void Notion_연동_시작시_인가_URL을_반환한다() throws Exception {
        when(integrationService.startNotionConnection(1L))
                .thenReturn(NotionAuthorizeResponse.of("https://api.notion.com/v1/oauth/authorize?client_id=x"));

        mockMvc.perform(post("/projects/{projectId}/integrations/notion", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorizeUrl").value("https://api.notion.com/v1/oauth/authorize?client_id=x"));
    }

    @Test
    void Notion_콜백은_프론트엔드로_리다이렉트한다() throws Exception {
        when(integrationService.handleNotionCallback("code123", "1"))
                .thenReturn("http://localhost:5173/projects/1/integrations?connected=notion");

        mockMvc.perform(get("/integrations/notion/callback")
                        .param("code", "code123")
                        .param("state", "1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/projects/1/integrations?connected=notion"));
    }

    @Test
    void GoogleMeet_연동을_시작한다() throws Exception {
        IntegrationStatus status = IntegrationStatus.connectGoogleMeet(1L);
        when(integrationService.connectGoogleMeet(1L)).thenReturn(IntegrationStatusResponse.from(status));

        mockMvc.perform(post("/projects/{projectId}/integrations/google-meet", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GOOGLE_MEET"))
                .andExpect(jsonPath("$.status").value("CONNECTED"));
    }

    @Test
    void 연동_상태_목록을_조회한다() throws Exception {
        IntegrationStatus notion = IntegrationStatus.connectNotion(1L, "token", "ws", "Workspace");
        when(integrationService.getStatus(1L)).thenReturn(List.of(IntegrationStatusResponse.from(notion)));

        mockMvc.perform(get("/projects/{projectId}/integrations/status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value(IntegrationType.NOTION.name()));
    }
}
