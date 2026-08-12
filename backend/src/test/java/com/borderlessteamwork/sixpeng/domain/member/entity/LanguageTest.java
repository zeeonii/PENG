package com.borderlessteamwork.sixpeng.domain.member.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageTest {

    @ParameterizedTest(name = "locale={0} -> KR")
    @CsvSource({"ko", "ko-KR", "ko_KR", "KO", "Ko-kr"})
    @DisplayName("한국어 locale 은 KR 로 판정한다")
    void koreanLocaleBecomesKr(String locale) {
        assertThat(Language.fromLocale(locale)).isEqualTo(Language.KR);
    }

    @ParameterizedTest(name = "locale={0} -> EN")
    @ValueSource(strings = {"en", "en-US", "ja", "fr-FR", "", "   ", "xx"})
    @DisplayName("판별할 수 없는 locale 은 시스템 기본값 EN 으로 떨어진다 (요구사항 5-7)")
    void unknownLocaleFallsBackToEn(String locale) {
        assertThat(Language.fromLocale(locale)).isEqualTo(Language.EN);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Google 이 locale 을 주지 않아도 EN 으로 떨어진다 (요구사항 5-7)")
    void nullLocaleFallsBackToEn(String locale) {
        assertThat(Language.fromLocale(locale)).isEqualTo(Language.EN);
    }
}
