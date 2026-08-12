package com.borderlessteamwork.sixpeng.domain.briefing.entity;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "briefing_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "briefing_id", nullable = false)
    private Briefing briefing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Builder
    public BriefingSource(Briefing briefing, Document document) {
        this.briefing = briefing;
        this.document = document;
    }
}
