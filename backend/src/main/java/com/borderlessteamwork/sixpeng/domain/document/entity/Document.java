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
    @Column(name = "source_id")
    private String sourceId;
    private String title;
    @Lob
    @Column(nullable = false)
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
