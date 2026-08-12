package com.borderlessteamwork.sixpeng.domain.qna.entity;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qna_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_history_id", nullable = false)
    private QnaHistory qnaHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Builder
    public QnaSource(QnaHistory qnaHistory, Document document) {
        this.qnaHistory = qnaHistory;
        this.document = document;
    }
}
