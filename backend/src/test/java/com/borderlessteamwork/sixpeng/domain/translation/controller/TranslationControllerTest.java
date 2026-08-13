package com.borderlessteamwork.sixpeng.domain.translation.controller;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TranslationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    TranslationService translationService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.ofGoogle(
                "g-translate", "translate@example.com", "번역러",
                com.borderlessteamwork.sixpeng.domain.member.entity.Language.KR));
    }

    /**
     * /translation은 GET이 없어서 "미로그인" 단독 케이스를 분리해 검증할 수 없다
     * (CSRF 필터가 인증 체크보다 먼저 동작해 토큰이 없으면 항상 403이 난다).
     * 그래서 아래 CSRF 테스트가 "미로그인 POST 차단"까지 같이 검증한다.
     */
    @Test
    @DisplayName("CSRF 토큰이 없으면 로그인했어도(=미로그인 포함) 403이다")
    void CSRF_토큰이_없으면_403을_반환한다() throws Exception {
        mockMvc.perform(post("/translation").with(TestLogin.withoutCsrf(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TranslationRequestFixture("안녕", Language.KR, Language.EN))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("번역 요청이 성공하면 200과 결과를 반환한다")
    void 번역_요청이_성공하면_200과_결과를_반환한다() throws Exception {
        when(translationService.translate(any())).thenReturn(TranslationResponse.of("안녕", "Hi"));

        mockMvc.perform(post("/translation").with(TestLogin.as(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TranslationRequestFixture("안녕", Language.KR, Language.EN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original").value("안녕"))
                .andExpect(jsonPath("$.translated").value("Hi"));
    }

    @Test
    @DisplayName("text가 비어있으면 400이다")
    void text가_비어있으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/translation").with(TestLogin.as(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TranslationRequestFixture("", null, null))))
                .andExpect(status().isBadRequest());
    }

    private record TranslationRequestFixture(String text, Language senderLanguage, Language receiverLanguage) {
    }
}
