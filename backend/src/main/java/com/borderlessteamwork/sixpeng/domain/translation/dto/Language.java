package com.borderlessteamwork.sixpeng.domain.translation.dto;

public enum Language {

    KR("ko"),
    EN("en");

    private final String googleCode;

    Language(String googleCode) {
        this.googleCode = googleCode;
    }

    public String getGoogleCode() {
        return googleCode;
    }

    public Language opposite() {
        return this == KR ? EN : KR;
    }

    public static Language fromGoogleCode(String googleCode) {
        for (Language language : values()) {
            if (language.googleCode.equalsIgnoreCase(googleCode)) {
                return language;
            }
        }
        return null;
    }
}
