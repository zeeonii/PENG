package com.borderlessteamwork.sixpeng.domain.qna.event;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.qna.service.DocumentEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEmbeddingEventListener {

    private final DocumentRepository documentRepository;
    private final DocumentEmbeddingService documentEmbeddingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentSaved(DocumentSavedEvent event) {
        documentRepository.findById(event.documentId()).ifPresentOrElse(
                document -> {
                    try {
                        documentEmbeddingService.embed(document);
                    } catch (Exception e) {
                        log.error("문서 임베딩 실패: documentId={}", document.getId(), e);
                    }
                },
                () -> log.warn("임베딩 대상 Document를 찾을 수 없습니다: documentId={}", event.documentId())
        );
    }
}
