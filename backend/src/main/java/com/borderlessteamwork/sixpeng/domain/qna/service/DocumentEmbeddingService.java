package com.borderlessteamwork.sixpeng.domain.qna.service;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private final OpenAiService openAiService;
    private final ChromaService chromaService;

    public void embed(Document document) {
        List<String> chunks = TextChunker.chunk(document.getContent());

        if (chunks.isEmpty()) {
            log.warn("임베딩할 내용이 없습니다: documentId={}", document.getId());
            return;
        }

        for (String chunk : chunks) {
            List<Double> embedding = openAiService.createEmbedding(chunk);
            chromaService.addEmbedding(document.getProjectId(), document.getId(), chunk, embedding);
        }

        log.info("문서 임베딩 완료: documentId={}, chunkCount={}", document.getId(), chunks.size());
    }
}
