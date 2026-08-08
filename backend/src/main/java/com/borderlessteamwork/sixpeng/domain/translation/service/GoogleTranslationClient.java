package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.HashMap;
import java.util.Map;

@Component
class GoogleTranslationClient {

    private final WebClient webClient;
    private final String apiKey;

    GoogleTranslationClient(
            WebClient.Builder webClientBuilder,
            @Value("${google.translate.base-url}") String baseUrl,
            @Value("${google.translate.api-key}") String apiKey
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    GoogleTranslateApiResponse.Translation translate(String text, String targetLanguage, String sourceLanguage) {
        Map<String, String> body = new HashMap<>();
        body.put("q", text);
        body.put("target", targetLanguage);
        body.put("format", "text");
        if (sourceLanguage != null) {
            body.put("source", sourceLanguage);
        }

        try {
            GoogleTranslateApiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GoogleTranslateApiResponse.class)
                    .block();

            if (response == null || response.data().translations().isEmpty()) {
                throw new BusinessException(ErrorCode.TRANSLATION_FAILED);
            }
            return response.data().translations().get(0);
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.TRANSLATION_FAILED);
        }
    }
}
