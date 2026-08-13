package com.borderlessteamwork.sixpeng.domain.integration.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import tools.jackson.databind.JsonNode;

/**
 * Google Docs API에서 문서 본문을 평문으로 읽어온다.
 * Google Meet 녹취록은 구조화된 entries API가 비어 있는 경우 Google Docs 파일로만
 * export되어 있을 수 있어(docsDestination), 이 클라이언트로 그 내용을 직접 읽는다.
 */
@Component
class GoogleDocsClient {

    private final WebClient webClient;

    GoogleDocsClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://docs.googleapis.com/v1").build();
    }

    /** 문서 id로 본문을 조회해 문단 텍스트를 이어붙인다. 실패하거나 내용이 없으면 빈 문자열. */
    String fetchPlainText(String accessToken, String documentId) {
        JsonNode response;
        try {
            response = webClient.get()
                    .uri("/documents/{documentId}", documentId)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientException e) {
            return "";
        }
        if (response == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode structuralElement : response.path("body").path("content")) {
            JsonNode elements = structuralElement.path("paragraph").path("elements");
            if (!elements.isArray()) {
                continue;
            }
            for (JsonNode element : elements) {
                String content = element.path("textRun").path("content").asString("");
                text.append(content);
            }
        }
        return text.toString();
    }
}
