package com.borderlessteamwork.sixpeng.domain.project.dto.response;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;

public record ProjectMemberResponse(
        Long memberId,
        String name,
        String role,
        String country,
        String timezone,
        String duty,
        /*
         * 프론트가 AI 팀원을 id 하드코딩 없이 구분할 수 있게 내려준다.
         * id 는 환경마다 달라지므로 판정 기준으로 쓰면 안 된다.
         */
        boolean isAiTeammate
) {

    public static ProjectMemberResponse from(ProjectMember projectMember) {
        Member member = projectMember.getMember();
        return new ProjectMemberResponse(
                member.getId(),
                member.getName(),
                projectMember.getRole(),
                member.getCountry(),
                member.getTimezone(),
                member.getDuty(),
                member.isAiTeammate()
        );
    }
}
