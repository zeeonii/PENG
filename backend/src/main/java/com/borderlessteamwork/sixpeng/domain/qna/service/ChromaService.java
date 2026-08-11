package com.borderlessteamwork.sixpeng.domain.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaService {

    private final WebClient chromaWebClient;

    private String collectionName(Long projectId) {
        return "project-" + projectId;
    }

    public void ensureCollection(Long projectId) {
        Map<String, Object> body = Map.of(
                "name", collectionName(projectId),
                "get_or_create", true
        );

        chromaWebClient.post()
                .uri("/api/v1/collections")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> log.error("Chroma collection 생성 실패: projectId={}", projectId, e))
                .block();
    }

    public void addEmbedding(Long projectId, Long documentId, String chunkText, List<Double> embedding) {
        ensureCollection(projectId);

        String chunkId = UUID.randomUUID().toString();

        Map<String, Object> body = Map.of(
                "ids", List.of(chunkId),
                "embeddings", List.of(embedding),
                "documents", List.of(chunkText),
                "metadatas", List.of(Map.of("document_id", documentId))
        );

        chromaWebClient.post()
                .uri("/api/v1/collections/{name}/add", collectionName(projectId))
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> log.error("Chroma 임베딩 추가 실패: documentId={}", documentId, e))
                .block();
    }

    public List<ChromaQueryResult> query(Long projectId, List<Double> questionEmbedding, int topK) {
        Map<String, Object> body = Map.of(
                "query_embeddings", List.of(questionEmbedding),
                "n_results", topK
        );

        ChromaQueryResponse response = chromaWebClient.post()
                .uri("/api/v1/collections/{name}/query", collectionName(projectId))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ChromaQueryResponse.class)
                .doOnError(e -> log.error("Chroma 검색 실패: projectId={}", projectId, e))
                .block();

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            return List.of();
        }

        List<String> docs = response.documents().get(0);
        List<Map<String, Object>> metas = response.metadatas().get(0);

        return java.util.stream.IntStream.range(0, docs.size())
                .mapToObj(i -> new ChromaQueryResult(
                        docs.get(i),
                        ((Number) metas.get(i).get("document_id")).longValue()
                ))
                .toList();
    }

    public record ChromaQueryResult(String chunkText, Long documentId) {}

    private record ChromaQueryResponse(
            List<List<String>> documents,
            List<List<Map<String, Object>>> metadatas
    ) {}
}
