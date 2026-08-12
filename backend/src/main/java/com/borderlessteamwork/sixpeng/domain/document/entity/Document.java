package com.borderlessteamwork.sixpeng.domain.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notion/Google Meet 등에서 수집한 원본 데이터. Context Q&A/브리핑의 검색 범위가 된다.
 */
@Entity
@Table(
        name = "document",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "source_type", "source_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private DocumentSourceType sourceType;

    /** 원본 시스템에서의 고유 id (예: Notion page id). 재동기화 시 중복 저장을 막는 기준. */
    @Column(name = "source_id")
    private String sourceId;

    private String title;

    /**
     * ddl-auto: update는 이미 만들어진 컬럼의 타입을 넓혀주지 못하므로, @Lob만으로는
     * 부족할 수 있어 컬럼 타입을 명시한다 (Notion 페이지 전체 텍스트 등 긴 내용 대비).
     */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    private Document(Long projectId, DocumentSourceType sourceType, String sourceId) {
        this.projectId = projectId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    public static Document collect(
            Long projectId, DocumentSourceType sourceType, String sourceId,
            String title, String content, String sourceUrl
    ) {
        Document document = new Document(projectId, sourceType, sourceId);
        document.updateContent(title, content, sourceUrl);
        return document;
    }

    public void updateContent(String title, String content, String sourceUrl) {
        this.title = title;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.collectedAt = LocalDateTime.now();
    }
}
