package com.borderlessteamwork.sixpeng.domain.member.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    private Member me;

    @BeforeEach
    void setUp() {
        me = memberRepository.save(Member.ofGoogle("google-1", "me@example.com", "서연", Language.KR));
    }

    @Test
    @DisplayName("GET /accounts/oauth/google 은 인증 없이 Google 인가 엔드포인트로 리다이렉트한다")
    void googleLoginRedirects() throws Exception {
        mockMvc.perform(get("/accounts/oauth/google"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/google"));
    }

    @Test
    @DisplayName("로그인하지 않으면 401 과 에러 코드를 내려준다")
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/accounts/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("C006"));
    }

    @Test
    @DisplayName("GET /accounts/me 는 로그인한 회원 정보를 반환한다")
    void getMe() throws Exception {
        mockMvc.perform(get("/accounts/me").with(TestLogin.as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(me.getId()))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.name").value("서연"))
                .andExpect(jsonPath("$.language").value("KR"));
    }

    @Test
    @DisplayName("PATCH /accounts/me 는 보낸 필드만 수정하고 나머지는 유지한다")
    void updateMePatchesOnlyGivenFields() throws Exception {
        mockMvc.perform(patch("/accounts/me").with(TestLogin.as(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"country": "KR", "timezone": "Asia/Seoul", "duty": "백엔드"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("KR"))
                .andExpect(jsonPath("$.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.duty").value("백엔드"))
                .andExpect(jsonPath("$.language").value("KR"));

        mockMvc.perform(patch("/accounts/me").with(TestLogin.as(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language": "EN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("EN"))
                .andExpect(jsonPath("$.duty").value("백엔드"));
    }

    @Test
    @DisplayName("허용되지 않은 language 값은 400 이다")
    void updateMeRejectsUnknownLanguage() throws Exception {
        mockMvc.perform(patch("/accounts/me").with(TestLogin.as(me))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language": "FR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("POST /accounts/logout 은 204 를 반환한다")
    void logout() throws Exception {
        mockMvc.perform(post("/accounts/logout").with(TestLogin.as(me)))
                .andExpect(status().isNoContent());
    }
}
