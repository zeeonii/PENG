package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;
import java.util.Map;

/** OpenAI Chat Completions API를 호출하는 범용 클라이언트. 번역 관련 프롬프트는 TranslationServiceImpl이 만든다. */
@Component
class OpenAiTranslationClient {

    private final WebClient webClient;
    private final String model;

    OpenAiTranslationClient(
            WebClient.Builder webClientBuilder,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
        this.model = model;
    }

    String complete(String systemPrompt, String userText) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userText)
                ),
                "temperature", 0.3
        );

        try {
            OpenAiChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OpenAiChatResponse.class)
                    .block();

            if (response == null || response.choices().isEmpty()) {
                throw new BusinessException(ErrorCode.TRANSLATION_FAILED);
            }
            return response.choices().get(0).message().content().trim();
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.TRANSLATION_FAILED);
        }
    }
}
