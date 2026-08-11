package com.borderlessteamwork.sixpeng.domain.translation.dto;

public enum Language {

    KR,
    EN;

    public Language opposite() {
        return this == KR ? EN : KR;
    }
}
