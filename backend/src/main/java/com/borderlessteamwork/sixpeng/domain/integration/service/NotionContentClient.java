package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Notion 콘텐츠 조회 전용 클라이언트. 인가 코드 교환은 {@link NotionOAuthClient}가 담당한다.
 * 응답 스키마가 유연해서(프로퍼티 타입에 따라 모양이 다름) 고정 타입 대신 JsonNode로 다룬다.
 */
@Component
class NotionContentClient {

    private static final String NOTION_VERSION = "2022-06-28";

    private final WebClient webClient;

    NotionContentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.notion.com/v1").build();
    }

    /** 이 Notion 연동(access token)이 접근 가능한 page들을 검색한다. 페이지네이션은 다루지 않고 첫 페이지만 가져온다. */
    List<NotionPage> searchAccessiblePages(String accessToken) {
        JsonNode response = post(accessToken, "/search",
                Map.of("filter", Map.of("value", "page", "property", "object")));

        List<NotionPage> pages = new ArrayList<>();
        JsonNode results = response.path("results");
        if (results.isArray()) {
            for (JsonNode page : results) {
                pages.add(new NotionPage(
                        page.path("id").asString(""),
                        extractTitle(page.path("properties")),
                        page.path("url").asString("")
                ));
            }
        }
        return pages;
    }

    /** page 본문의 최상위 블록 텍스트만 이어붙인다 (중첩된 하위 블록은 재귀 조회하지 않는 MVP 범위). */
    String fetchPageContent(String accessToken, String pageId) {
        JsonNode response = get(accessToken, "/blocks/" + pageId + "/children?page_size=100");

        StringBuilder content = new StringBuilder();
        JsonNode results = response.path("results");
        if (results.isArray()) {
            for (JsonNode block : results) {
                appendBlockText(block, content);
            }
        }
        return content.toString();
    }

    private void appendBlockText(JsonNode block, StringBuilder content) {
        String type = block.path("type").asString("");
        JsonNode richTextArray = block.path(type).path("rich_text");
        if (richTextArray.isArray()) {
            for (JsonNode richText : richTextArray) {
                content.append(richText.path("plain_text").asString(""));
            }
            content.append(System.lineSeparator());
        }
    }

    private String extractTitle(JsonNode properties) {
        for (Map.Entry<String, JsonNode> property : properties.properties()) {
            JsonNode value = property.getValue();
            if ("title".equals(value.path("type").asString(""))) {
                StringBuilder title = new StringBuilder();
                for (JsonNode richText : value.path("title")) {
                    title.append(richText.path("plain_text").asString(""));
                }
                return title.toString();
            }
        }
        return "";
    }

    private JsonNode post(String accessToken, String uri, Object body) {
        try {
            JsonNode response = webClient.post()
                    .uri(uri)
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.set("Notion-Version", NOTION_VERSION);
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (response == null) {
                throw new BusinessException(ErrorCode.NOTION_SYNC_FAILED);
            }
            return response;
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.NOTION_SYNC_FAILED);
        }
    }

    private JsonNode get(String accessToken, String uri) {
        try {
            JsonNode response = webClient.get()
                    .uri(uri)
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.set("Notion-Version", NOTION_VERSION);
                    })
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (response == null) {
                throw new BusinessException(ErrorCode.NOTION_SYNC_FAILED);
            }
            return response;
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.NOTION_SYNC_FAILED);
        }
    }
}
