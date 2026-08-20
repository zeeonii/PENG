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
import java.util.function.Function;

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

    /**
     * 이 Notion 연동(access token)이 접근 가능한 page들을 검색한다. 페이지네이션은 다루지 않고 첫 페이지만 가져온다.
     *
     * <p>Notion 데이터베이스(표)의 각 행도 그 자체로 하나의 page(parent가 database_id인)라서
     * 이 필터에 걸린다. 다만 표 안에 입력한 내용은 본문 block이 아니라 그 행 page의
     * properties(표의 각 컬럼)에 들어있어서, 본문만 읽는 fetchPageContent만으로는 비어
     * 보인다. 그래서 여기서 search 응답에 이미 포함된 properties를 텍스트로 함께 뽑아둔다.
     */
    List<NotionPage> searchAccessiblePages(String accessToken) {
        JsonNode response = post(accessToken, "/search",
                Map.of("filter", Map.of("value", "page", "property", "object")));

        List<NotionPage> pages = new ArrayList<>();
        JsonNode results = response.path("results");
        if (results.isArray()) {
            for (JsonNode page : results) {
                JsonNode properties = page.path("properties");
                pages.add(new NotionPage(
                        page.path("id").asString(""),
                        extractTitle(properties),
                        page.path("url").asString(""),
                        extractPropertiesText(properties)
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
                return joinRichText(value.path("title"));
            }
        }
        return "";
    }

    /** 데이터베이스(표)의 한 행이 가진 속성(컬럼)값들을 "컬럼명: 값" 줄들로 합친다. */
    private String extractPropertiesText(JsonNode properties) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, JsonNode> property : properties.properties()) {
            String value = extractPropertyValueText(property.getValue());
            if (!value.isBlank()) {
                text.append(property.getKey()).append(": ").append(value).append(System.lineSeparator());
            }
        }
        return text.toString();
    }

    private String extractPropertyValueText(JsonNode property) {
        String type = property.path("type").asString("");
        JsonNode value = property.path(type);
        return switch (type) {
            case "title", "rich_text" -> joinRichText(value);
            case "select", "status" -> value.path("name").asString("");
            case "multi_select" -> joinArray(value, item -> item.path("name").asString(""));
            case "people" -> joinArray(value, item -> item.path("name").asString(""));
            case "date" -> value.path("start").asString("");
            case "checkbox" -> value.asBoolean(false) ? "true" : "";
            case "number", "url", "email", "phone_number" -> value.isNull() ? "" : value.asString("");
            default -> "";
        };
    }

    private String joinRichText(JsonNode richTextArray) {
        StringBuilder text = new StringBuilder();
        if (richTextArray.isArray()) {
            for (JsonNode richText : richTextArray) {
                text.append(richText.path("plain_text").asString(""));
            }
        }
        return text.toString();
    }

    private String joinArray(JsonNode array, Function<JsonNode, String> extractor) {
        if (!array.isArray()) {
            return "";
        }
        StringBuilder joined = new StringBuilder();
        for (JsonNode item : array) {
            if (!joined.isEmpty()) {
                joined.append(", ");
            }
            joined.append(extractor.apply(item));
        }
        return joined.toString();
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
