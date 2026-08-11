package com.borderlessteamwork.sixpeng.domain.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private static final String EMBEDDING_MODEL = "text-embedding-3-small";
    private static final String CHAT_MODEL = "gpt-4o-mini";

    private final WebClient openAiWebClient;

    public List<Double> createEmbedding(String text) {
        Map<String, Object> body = Map.of(
                "model", EMBEDDING_MODEL,
                "input", text
        );

        EmbeddingResponse response = openAiWebClient.post()
                .uri("/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .doOnError(e -> log.error("OpenAI 임베딩 생성 실패", e))
                .block();

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("OpenAI 임베딩 응답이 비어있습니다.");
        }

        return response.data().get(0).embedding();
    }

    public String generateAnswer(String question, List<String> contextChunks) {
        String contextText = contextChunks.isEmpty()
                ? "(관련 문서 없음)"
                : String.join("\n---\n", contextChunks);

        String systemPrompt = """
                당신은 팀의 프로젝트 문서를 기억하는 AI 팀원입니다.
                아래 제공된 근거 문서만을 바탕으로 질문에 답변하세요.
                근거 문서에 명확한 답이 없으면, 절대로 추측하지 말고
                "확실한 근거를 찾지 못했습니다"라고 답하세요.
                답변은 간결하고 한국어로 작성하세요.
                """;

        String userPrompt = """
                [근거 문서]
                %s

                [질문]
                %s
                """.formatted(contextText, question);

        Map<String, Object> body = Map.of(
                "model", CHAT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );

        ChatCompletionResponse response = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .doOnError(e -> log.error("OpenAI 답변 생성 실패", e))
                .block();

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI 답변 응답이 비어있습니다.");
        }

        return response.choices().get(0).message().content();
    }

    private record EmbeddingResponse(List<EmbeddingData> data) {}
    private record EmbeddingData(List<Double> embedding) {}

    private record ChatCompletionResponse(List<Choice> choices) {}
    private record Choice(ChatMessage message) {}
    private record ChatMessage(String role, String content) {}
}
