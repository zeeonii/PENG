package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record GoogleTranslateApiResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Data(List<Translation> translations) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Translation(String translatedText, String detectedSourceLanguage) {
    }
}
