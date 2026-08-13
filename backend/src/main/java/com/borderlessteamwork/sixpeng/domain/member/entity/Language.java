package com.borderlessteamwork.sixpeng.domain.member.entity;

public enum Language {

    KR,
    EN,
    ;

    /**
     * Google 프로필의 locale('ko', 'ko-KR', 'en-US' 등)로부터 기본 언어를 정한다.
     * 판별할 수 없으면 EN 으로 둔다.
     */
    public static Language fromLocale(String locale) {
        if (locale != null && locale.toLowerCase().startsWith("ko")) {
            return KR;
        }
        return EN;
    }
}
