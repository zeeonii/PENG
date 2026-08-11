package com.borderlessteamwork.sixpeng.domain.qna.dto.response;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QnaResponse {

    private Long id;
    private String question;
    private String answer;
    private LocalDateTime createdAt;
    private List<SourceItem> sources;

    public static QnaResponse of(QnaHistory qnaHistory, List<SourceItem> sources) {
        return QnaResponse.builder()
                .id(qnaHistory.getId())
                .question(qnaHistory.getQuestion())
                .answer(qnaHistory.getAnswer())
                .createdAt(qnaHistory.getCreatedAt())
                .sources(sources)
                .build();
    }

    @Getter
    @Builder
    public static class SourceItem {
        private Long documentId;
        private String title;
        private String sourceUrl;
    }
}
