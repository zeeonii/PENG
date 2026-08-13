package com.borderlessteamwork.sixpeng.domain.member.controller;

import com.borderlessteamwork.sixpeng.domain.member.dto.request.MemberUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.member.dto.response.MemberResponse;
import com.borderlessteamwork.sixpeng.domain.member.service.MemberService;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final URI GOOGLE_AUTHORIZATION_URI = URI.create("/oauth2/authorization/google");

    private final MemberService memberService;

    /**
     * Google 로그인 진입점. Spring Security 의 authorization 엔드포인트로 넘긴다.
     * 로그인이 끝나면 app.oauth2.success-redirect-uri 로 돌아가고, 이후 GET /accounts/me 로 회원 정보를 받는다.
     * <p>
     * POST /accounts/logout 은 Spring Security 의 logout 필터가 처리한다(204 No Content).
     */
    @GetMapping("/oauth/google")
    public ResponseEntity<Void> googleLogin() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, GOOGLE_AUTHORIZATION_URI.toString())
                .build();
    }

    @GetMapping("/me")
    public MemberResponse getMe(@CurrentMember Long memberId) {
        return MemberResponse.from(memberService.findById(memberId));
    }

    @PatchMapping("/me")
    public MemberResponse updateMe(@CurrentMember Long memberId,
                                   @Valid @RequestBody MemberUpdateRequest request) {
        return MemberResponse.from(memberService.updateProfile(memberId, request));
    }
}
