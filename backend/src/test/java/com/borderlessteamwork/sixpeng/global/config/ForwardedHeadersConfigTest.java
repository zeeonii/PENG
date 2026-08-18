package com.borderlessteamwork.sixpeng.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Railway 처럼 TLS 를 프록시에서 끊는 환경에서는 컨테이너 안으로 http 요청이 들어온다.
 * X-Forwarded-* 를 신뢰하지 않으면 OAuth redirect_uri 가 http:// 로 생성돼
 * Google 이 redirect_uri_mismatch 로 거절한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ForwardedHeadersConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("X-Forwarded-Proto 를 반영해 redirect_uri 를 https 로 만든다")
    void buildsHttpsRedirectUriBehindProxy() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/google")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Host", "peng-production-54a9.up.railway.app"))
                .andReturn();

        String location = URLDecoder.decode(
                String.valueOf(result.getResponse().getRedirectedUrl()), StandardCharsets.UTF_8);

        assertThat(location)
                .contains("redirect_uri=https://peng-production-54a9.up.railway.app/login/oauth2/code/google");
    }

    @Test
    @DisplayName("프록시 헤더가 없으면 요청 스킴 그대로 http 를 쓴다")
    void keepsHttpWithoutProxyHeaders() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/authorization/google")).andReturn();

        String location = URLDecoder.decode(
                String.valueOf(result.getResponse().getRedirectedUrl()), StandardCharsets.UTF_8);

        assertThat(location).contains("redirect_uri=http://localhost/login/oauth2/code/google");
    }

    /**
     * 한 번 prod 프로파일 문서 안에 들어가 있어서 배포에서 동작하지 않은 적이 있다.
     * 테스트는 test 클래스패스의 application.yml 로 돌기 때문에 위 두 테스트로는
     * main 설정의 위치를 검증할 수 없어, 파일 자체를 확인한다.
     */
    @Test
    @DisplayName("main application.yml 에서 forward-headers-strategy 는 prod 전용이 아니라 기본 문서에 있다")
    void forwardHeadersStrategyLivesInDefaultDocument() throws Exception {
        Path yml = Path.of("src/main/resources/application.yml");
        assertThat(yml).as("테스트 작업 디렉터리 기준으로 application.yml 을 찾지 못했다").exists();

        String[] documents = Files.readString(yml).split("(?m)^---\\s*$");

        assertThat(documents[0])
                .as("기본 문서에 있어야 프로파일과 무관하게 적용된다")
                .contains("forward-headers-strategy");
    }
}
