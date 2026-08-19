package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Google Meet/Drive 접근 권한만 추가로 요청하는 incremental authorization 흐름.
 * 로그인(spring.security.oauth2...google)과 같은 Client ID/Secret을 재사용하되,
 * 로그인 스코프(profile/email)와는 별개로 Meet 전용 스코프를 이 흐름에서만 요청한다.
 */
@Component
class GoogleMeetOAuthClient {

    /**
     * drive.meet.readonly로는 녹취록이 export된 Google Docs 파일을 Drive API로 읽으려 하면
     * 404가 발생함을 실제 연동 테스트로 확인했다 (권한이 있어도 일반 Drive 엔드포인트는 막혀 있는 것으로 보임).
     * entries API가 비어 있을 때 Docs API로 직접 읽기 위해 documents.readonly로 교체한다.
     */
    private static final String SCOPES = String.join(" ",
            "https://www.googleapis.com/auth/meetings.space.readonly",
            "https://www.googleapis.com/auth/documents.readonly"
    );

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authorizeUri;

    GoogleMeetOAuthClient(
            WebClient.Builder webClientBuilder,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret,
            @Value("${google-meet.redirect-uri}") String redirectUri,
            @Value("${google-meet.authorize-uri}") String authorizeUri,
            @Value("${google-meet.token-uri}") String tokenUri
    ) {
        this.webClient = webClientBuilder.baseUrl(tokenUri).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.authorizeUri = authorizeUri;
    }

    String buildAuthorizeUrl(Long projectId) {
        return UriComponentsBuilder.fromUriString(authorizeUri)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", SCOPES)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                // Notion 쪽에서 state가 순수 숫자 문자열이면 거부하는 걸 겪어서, 두 provider 모두
                // 같은 형식(접두사 p)으로 통일해 둔다.
                .queryParam("state", "p" + projectId)
                .encode()
                .build()
                .toUriString();
    }

    GoogleTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);

        try {
            GoogleTokenResponse response = webClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null) {
                throw new BusinessException(ErrorCode.GOOGLE_MEET_AUTH_FAILED);
            }
            return response;
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.GOOGLE_MEET_AUTH_FAILED);
        }
    }
}
