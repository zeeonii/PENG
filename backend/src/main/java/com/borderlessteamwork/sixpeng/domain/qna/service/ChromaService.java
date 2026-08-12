package com.borderlessteamwork.sixpeng.domain.qna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaService {

    private static final String TENANT = "default_tenant";
    private static final String DATABASE = "default_database";

    private final WebClient chromaWebClient;

    private final Map<Long, String> collectionIdCache = new ConcurrentHashMap<>();

    private String collectionsBasePath() {
        return "/api/v2/tenants/" + TENANT + "/databases/" + DATABASE + "/collections";
    }

    private String collectionName(Long projectId) {
        return "project-" + projectId;
    }

    private String getOrCreateCollectionId(Long projectId) {
        return collectionIdCache.computeIfAbsent(projectId, id -> {
            Map<String, Object> body = Map.of(
                    "name", collectionName(id),
                    "get_or_create", true
            );

            ChromaCollection collection = chromaWebClient.post()
                    .uri(collectionsBasePath())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ChromaCollection.class)
                    .doOnError(e -> log.error("Chroma collection 생성 실패: projectId={}", id, e))
                    .block();

            if (collection == null || collection.id() == null) {
                throw new IllegalStateException("Chroma collection 생성 응답이 비어있습니다: projectId=" + id);
            }
            return collection.id();
        });
    }

    public void addEmbedding(Long projectId, Long documentId, String chunkText, List<Double> embedding) {
        String collectionId = getOrCreateCollectionId(projectId);
        String chunkId = UUID.randomUUID().toString();

        Map<String, Object> body = Map.of(
                "ids", List.of(chunkId),
                "embeddings", List.of(embedding),
                "documents", List.of(chunkText),
                "metadatas", List.of(Map.of("document_id", documentId))
        );

        chromaWebClient.post()
                .uri(collectionsBasePath() + "/{collectionId}/add", collectionId)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> log.error("Chroma 임베딩 추가 실패: documentId={}", documentId, e))
                .block();
    }

    public List<ChromaQueryResult> query(Long projectId, List<Double> questionEmbedding, int topK) {
        String collectionId = getOrCreateCollectionId(projectId);

        Map<String, Object> body = Map.of(
                "query_embeddings", List.of(questionEmbedding),
                "n_results", topK
        );

        ChromaQueryResponse response = chromaWebClient.post()
                .uri(collectionsBasePath() + "/{collectionId}/query", collectionId)
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

    private record ChromaCollection(String id, String name) {}

    private record ChromaQueryResponse(
            List<List<String>> documents,
            List<List<Map<String, Object>>> metadatas
    ) {}
}
