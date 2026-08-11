package com.borderlessteamwork.sixpeng.domain.document.entity;

import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String sourceType;

    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    private String sourceUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    @PrePersist
    protected void onCreate() {
        this.collectedAt = LocalDateTime.now();
    }
}
