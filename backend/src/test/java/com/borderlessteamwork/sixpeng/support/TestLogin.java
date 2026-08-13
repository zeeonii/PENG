package com.borderlessteamwork.sixpeng.support;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.global.security.CustomOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * CustomOAuth2UserService 를 거치지 않고, 로그인이 끝난 상태의 principal 을 직접 심는다.
 */
public final class TestLogin {

    private TestLogin() {
    }

    /** 로그인 + CSRF 토큰까지, 브라우저가 보내는 것과 같은 상태로 만든다. */
    public static RequestPostProcessor as(Member member) {
        RequestPostProcessor csrf = csrf();
        return request -> csrf.postProcessRequest(withoutCsrf(member).postProcessRequest(request));
    }

    /** CSRF 토큰 없이 로그인만 한 상태. CSRF 방어가 실제로 동작하는지 확인할 때 쓴다. */
    public static RequestPostProcessor withoutCsrf(Member member) {
        CustomOAuth2User principal = new CustomOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", member.getGoogleId()),
                "sub",
                member.getId()
        );
        return authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google"));
    }
}
