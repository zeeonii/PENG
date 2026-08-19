package com.borderlessteamwork.sixpeng.domain.briefing.dto.response;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.BriefingSource;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BriefingResponse {

    private Long briefingId;
    private String summary;
    private LocalDateTime createdAt;
    private List<SourceItem> sources;

    private long newMeetingCount;
    private long documentChangeCount;

    public static BriefingResponse of(Briefing briefing, List<BriefingSource> sources) {
        List<SourceItem> sourceItems = sources.stream().map(SourceItem::from).toList();

        long meetingCount = sources.stream()
                .filter(s -> s.getDocument().getSourceType() == DocumentSourceType.GOOGLE_MEET)
                .count();
        long documentCount = sources.stream()
                .filter(s -> s.getDocument().getSourceType() == DocumentSourceType.NOTION)
                .count();

        return BriefingResponse.builder()
                .briefingId(briefing.getId())
                .summary(briefing.getSummary())
                .createdAt(briefing.getCreatedAt())
                .sources(sourceItems)
                .newMeetingCount(meetingCount)
                .documentChangeCount(documentCount)
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
