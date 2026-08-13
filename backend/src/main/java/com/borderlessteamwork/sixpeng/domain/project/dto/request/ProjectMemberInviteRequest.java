package com.borderlessteamwork.sixpeng.domain.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 초대 대상은 이미 Google 로그인으로 가입한 회원이어야 한다(별도 초대 테이블 없음).
 */
public record ProjectMemberInviteRequest(
        @NotBlank
        @Email
        String email,

        @Size(max = 255)
        String role
) {
}
