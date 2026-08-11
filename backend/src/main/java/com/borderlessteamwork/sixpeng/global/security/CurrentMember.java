package com.borderlessteamwork.sixpeng.global.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인한 회원의 member.id 를 컨트롤러 파라미터로 주입한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "memberId")
public @interface CurrentMember {
}
