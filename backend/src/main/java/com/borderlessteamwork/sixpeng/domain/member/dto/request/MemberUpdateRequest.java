package com.borderlessteamwork.sixpeng.domain.member.dto.request;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;

/**
 * 프로필 수정 요청. 모든 필드가 선택값이며, 생략(null)한 필드는 변경되지 않는다.
 */
public record MemberUpdateRequest(
        Language language,
        String country,
        String timezone,
        String duty
) {
}
