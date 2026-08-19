package com.borderlessteamwork.sixpeng.support;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.global.security.CustomOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * CustomOAuth2UserService 를 거치지 않고, 로그인이 끝난 상태의 principal 을 직접 심는다.
 */
public final class TestLogin {

    private TestLogin() {
    }

    /** 로그인이 끝난 상태의 principal 을 심는다. */
    public static RequestPostProcessor as(Member member) {
        CustomOAuth2User principal = new CustomOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", member.getGoogleId()),
                "sub",
                member.getId()
        );
        return authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google"));
    }
}
