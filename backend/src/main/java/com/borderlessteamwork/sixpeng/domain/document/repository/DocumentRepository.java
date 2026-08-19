package com.borderlessteamwork.sixpeng.domain.document.repository;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByProjectIdAndSourceTypeAndSourceId(
            Long projectId, DocumentSourceType sourceType, String sourceId);

    List<Document> findByProjectIdAndCollectedAtAfterOrderByCollectedAtAsc(Long projectId, LocalDateTime after);
}
