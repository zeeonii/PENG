package com.borderlessteamwork.sixpeng.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${chroma.base-url}")
    private String chromaBaseUrl;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient chromaWebClient() {
        return WebClient.builder()
                .baseUrl(chromaBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
