package com.borderlessteamwork.sixpeng.domain.activity.dto.response;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;

import java.time.LocalDateTime;

/** 프로젝트 홈 화면의 "최근 활동" 피드 항목. document/briefing 도메인 데이터를 시간순으로 합친다. */
public record ActivityResponse(
        String type,
        String description,
        LocalDateTime occurredAt
) {

    public static ActivityResponse fromDocument(Document document) {
        String description = document.getSourceType() == DocumentSourceType.GOOGLE_MEET
                ? "회의록이 업데이트되었습니다: " + document.getTitle()
                : "Notion 문서에 \"" + document.getTitle() + "\"이(가) 추가되었습니다";
        return new ActivityResponse("DOCUMENT_COLLECTED", description, document.getCollectedAt());
    }

    public static ActivityResponse fromBriefing(Briefing briefing) {
        return new ActivityResponse("BRIEFING_GENERATED", "오늘의 브리핑이 생성되었습니다.", briefing.getCreatedAt());
    }
}
