package com.borderlessteamwork.sixpeng.domain.project.dto.response;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;

public record ProjectMemberResponse(
        Long memberId,
        String name,
        String role,
        String country,
        String timezone
) {

    public static ProjectMemberResponse from(ProjectMember projectMember) {
        Member member = projectMember.getMember();
        return new ProjectMemberResponse(
                member.getId(),
                member.getName(),
                projectMember.getRole(),
                member.getCountry(),
                member.getTimezone()
        );
    }
}
