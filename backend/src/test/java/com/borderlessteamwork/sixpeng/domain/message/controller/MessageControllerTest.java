package com.borderlessteamwork.sixpeng.domain.message.controller;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import com.borderlessteamwork.sixpeng.domain.message.service.MessageService;
import com.borderlessteamwork.sixpeng.support.TestLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MessageService는 목으로 대체해 컨트롤러 계층만 검증한다 (실제 TranslationService는
 * 외부 API를 호출하므로 여기서 태우지 않는다). 로그인/CSRF는 TestLogin으로 실제 Security
 * 필터 체인을 그대로 통과시켜 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    MessageService messageService;

    private Member sender;

    @BeforeEach
    void setUp() {
        sender = memberRepository.save(Member.ofGoogle("g-sender", "sender@example.com", "보낸사람", Language.KR));
    }

    @Test
    @DisplayName("로그인하지 않으면 401이다")
    void 로그인하지_않으면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 쪽지 목록을 조회한다")
    void 내_쪽지_목록을_조회한다() throws Exception {
        Message message = Message.create(1L, sender.getId(), 20L, "hi", "안녕");
        when(messageService.getMessages(sender.getId())).thenReturn(List.of(MessageResponse.from(message)));

        mockMvc.perform(get("/messages").with(TestLogin.as(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalText").value("hi"));
    }

    @Test
    @DisplayName("쪽지를 전송하면 201이다")
    void 쪽지를_전송하면_201을_반환한다() throws Exception {
        Message message = Message.create(1L, sender.getId(), 20L, "hi", "안녕");
        when(messageService.sendMessage(eq(sender.getId()), any())).thenReturn(MessageResponse.from(message));

        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("hi");

        mockMvc.perform(post("/messages").with(TestLogin.as(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(sender.getId()))
                .andExpect(jsonPath("$.isRead").value(false));
    }

    @Test
    @DisplayName("로그인하지 않으면 쪽지를 보낼 수 없다")
    void 로그인하지_않으면_쪽지를_보낼_수_없다() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("hi");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("originalText가 비어있으면 400이다")
    void originalText가_비어있으면_400을_반환한다() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("");

        mockMvc.perform(post("/messages").with(TestLogin.as(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("쪽지를 읽음 처리하면 200이다")
    void 쪽지를_읽음처리하면_200을_반환한다() throws Exception {
        mockMvc.perform(patch("/messages/{messageId}/read", 1L).with(TestLogin.as(sender)))
                .andExpect(status().isOk());
    }
}
