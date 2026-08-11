package com.borderlessteamwork.sixpeng.domain.briefing.dto.response;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BriefingSummaryResponse {

    private Long id;
    private String summary;
    private LocalDateTime createdAt;

    public static BriefingSummaryResponse from(Briefing briefing) {
        return BriefingSummaryResponse.builder()
                .id(briefing.getId())
                .summary(briefing.getSummary())
                .createdAt(briefing.getCreatedAt())
                .build();
    }
}
