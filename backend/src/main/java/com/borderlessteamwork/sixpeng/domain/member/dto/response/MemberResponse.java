package com.borderlessteamwork.sixpeng.domain.member.dto.response;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;

public record MemberResponse(
        Long id,
        String email,
        String name,
        Language language,
        String country,
        String timezone,
        String duty
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getLanguage(),
                member.getCountry(),
                member.getTimezone(),
                member.getDuty()
        );
    }
}
