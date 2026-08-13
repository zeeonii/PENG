package com.borderlessteamwork.sixpeng.domain.briefing.dto.response;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.BriefingSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BriefingDetailResponse {

    private Long id;
    private String summary;
    private LocalDateTime createdAt;
    private List<SourceItem> sources;

    public static BriefingDetailResponse of(Briefing briefing, List<BriefingSource> sources) {
        return BriefingDetailResponse.builder()
                .id(briefing.getId())
                .summary(briefing.getSummary())
                .createdAt(briefing.getCreatedAt())
                .sources(sources.stream().map(SourceItem::from).toList())
                .build();
    }

    @Getter
    @Builder
    public static class SourceItem {
        private Long documentId;
        private String title;
        private String sourceType;
        private String sourceUrl;

        public static SourceItem from(BriefingSource briefingSource) {
            var document = briefingSource.getDocument();
            return SourceItem.builder()
                    .documentId(document.getId())
                    .title(document.getTitle())
                    .sourceType(document.getSourceType().name())
                    .sourceUrl(document.getSourceUrl())
                    .build();
        }
    }
}
