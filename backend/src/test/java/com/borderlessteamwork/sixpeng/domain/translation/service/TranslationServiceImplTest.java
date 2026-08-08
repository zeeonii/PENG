package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceImplTest {

    @Mock
    GoogleTranslationClient googleTranslationClient;

    @InjectMocks
    TranslationServiceImpl translationService;

    @Test
    void 발신자_수신자_언어가_모두_지정되면_그대로_번역한다() {
        when(googleTranslationClient.translate("안녕하세요", "en", "ko"))
                .thenReturn(new GoogleTranslateApiResponse.Translation("Hello", "ko"));

        TranslationResponse response = translationService.translate(
                new TranslationRequest("안녕하세요", Language.KR, Language.EN)
        );

        assertThat(response.getOriginal()).isEqualTo("안녕하세요");
        assertThat(response.getTranslated()).isEqualTo("Hello");
    }

    @Test
    void 발신자_언어만_지정되면_반대_언어로_번역한다() {
        when(googleTranslationClient.translate("Hello", "ko", "en"))
                .thenReturn(new GoogleTranslateApiResponse.Translation("안녕하세요", "en"));

        TranslationResponse response = translationService.translate(
                new TranslationRequest("Hello", Language.EN, null)
        );

        assertThat(response.getTranslated()).isEqualTo("안녕하세요");
    }

    @Test
    void 언어_미지정_시_한국어가_감지되면_한번만_호출해서_영어로_번역한다() {
        when(googleTranslationClient.translate(eq("안녕하세요"), eq("en"), isNull()))
                .thenReturn(new GoogleTranslateApiResponse.Translation("Hello", "ko"));

        TranslationResponse response = translationService.translate(
                new TranslationRequest("안녕하세요", null, null)
        );

        assertThat(response.getTranslated()).isEqualTo("Hello");
        verify(googleTranslationClient, never()).translate(any(), eq("ko"), any());
    }

    @Test
    void 언어_미지정_시_영어가_감지되면_한국어로_재번역한다() {
        when(googleTranslationClient.translate(eq("Hello"), eq("en"), isNull()))
                .thenReturn(new GoogleTranslateApiResponse.Translation("Hello", "en"));
        when(googleTranslationClient.translate("Hello", "ko", "en"))
                .thenReturn(new GoogleTranslateApiResponse.Translation("안녕하세요", "en"));

        TranslationResponse response = translationService.translate(
                new TranslationRequest("Hello", null, null)
        );

        assertThat(response.getTranslated()).isEqualTo("안녕하세요");
    }

    @Test
    void 외부_API_호출이_실패하면_BusinessException으로_전파된다() {
        when(googleTranslationClient.translate(any(), any(), any()))
                .thenThrow(new BusinessException(com.borderlessteamwork.sixpeng.global.exception.ErrorCode.TRANSLATION_FAILED));

        assertThatThrownBy(() -> translationService.translate(new TranslationRequest("hi", Language.EN, Language.KR)))
                .isInstanceOf(BusinessException.class);
    }
}
