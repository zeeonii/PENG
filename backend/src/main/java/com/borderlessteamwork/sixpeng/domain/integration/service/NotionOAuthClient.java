package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
class NotionOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(NotionOAuthClient.class);

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authorizeUri;

    NotionOAuthClient(
            WebClient.Builder webClientBuilder,
            @Value("${notion.client-id}") String clientId,
            @Value("${notion.client-secret}") String clientSecret,
            @Value("${notion.redirect-uri}") String redirectUri,
            @Value("${notion.authorize-uri}") String authorizeUri,
            @Value("${notion.token-uri}") String tokenUri
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
                .queryParam("owner", "user")
                .queryParam("redirect_uri", redirectUri)
                // Notion이 state 값이 숫자로만 이루어져 있으면 "string이어야 하는데 숫자였다"며 거부해서
                // 접두사를 붙여 순수 숫자 문자열이 되지 않게 한다.
                .queryParam("state", "p" + projectId)
                .encode()
                .build()
                .toUriString();
    }

    NotionTokenResponse exchangeCodeForToken(String code) {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", redirectUri);

        try {
            NotionTokenResponse response = webClient.post()
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(NotionTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null) {
                log.warn("Notion 토큰 교환 실패: 응답에 access_token 없음, response={}", response);
                throw new BusinessException(ErrorCode.NOTION_AUTH_FAILED);
            }
            return response;
        } catch (WebClientResponseException e) {
            log.warn("Notion 토큰 교환 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.NOTION_AUTH_FAILED);
        } catch (WebClientException e) {
            log.warn("Notion 토큰 교환 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOTION_AUTH_FAILED);
        }
    }
}
