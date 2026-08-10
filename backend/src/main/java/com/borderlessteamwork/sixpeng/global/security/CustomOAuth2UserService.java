package com.borderlessteamwork.sixpeng.global.security;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Google userinfo 응답을 받아 회원을 등록/갱신하고, member.id 를 담은 principal 을 만든다.
 * <p>
 * scope 에 openid 를 넣으면 Spring Security 가 OIDC 경로(OidcUserService)를 타므로,
 * application.yml 에서는 profile/email 만 요청한다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ATTRIBUTE_SUB = "sub";
    private static final String ATTRIBUTE_EMAIL = "email";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_LOCALE = "locale";

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String googleId = stringAttribute(attributes, ATTRIBUTE_SUB);
        String email = stringAttribute(attributes, ATTRIBUTE_EMAIL);
        String name = stringAttribute(attributes, ATTRIBUTE_NAME);
        if (googleId == null || email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"), "Google 계정에서 sub/email 을 받지 못했습니다.");
        }

        Member member = memberService.upsertGoogleMember(
                googleId,
                email,
                name != null ? name : email,
                Language.fromLocale(stringAttribute(attributes, ATTRIBUTE_LOCALE))
        );

        return new CustomOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                ATTRIBUTE_SUB,
                member.getId()
        );
    }

    private String stringAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value instanceof String s && !s.isBlank() ? s : null;
    }
}
