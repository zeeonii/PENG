package com.borderlessteamwork.sixpeng.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * 세션에 저장되는 인증 주체. Google 속성에 더해 우리 DB 의 member.id 를 들고 다닌다.
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final Long memberId;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            Long memberId) {
        super(authorities, attributes, nameAttributeKey);
        this.memberId = memberId;
    }
}
