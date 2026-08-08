package com.borderlessteamwork.sixpeng.domain.translation.dto.response;

import lombok.Getter;

@Getter
public class TranslationResponse {

    private final String original;
    private final String translated;

    private TranslationResponse(String original, String translated) {
        this.original = original;
        this.translated = translated;
    }

    public static TranslationResponse of(String original, String translated) {
        return new TranslationResponse(original, translated);
    }
}
