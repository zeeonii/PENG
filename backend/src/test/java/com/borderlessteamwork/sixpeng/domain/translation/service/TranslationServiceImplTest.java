package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceImplTest {

    @Mock
    OpenAiTranslationClient openAiTranslationClient;

    @InjectMocks
    TranslationServiceImpl translationService;

    @Test
    void 발신자_수신자_언어가_모두_지정되면_대상_문화권_뉘앙스를_프롬프트에_반영한다() {
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(openAiTranslationClient.complete(promptCaptor.capture(), anyString())).thenReturn("Hello");

        TranslationResponse response = translationService.translate(
                new TranslationRequest("안녕하세요", Language.KR, Language.EN)
        );

        assertThat(response.getOriginal()).isEqualTo("안녕하세요");
        assertThat(response.getTranslated()).isEqualTo("Hello");
        assertThat(promptCaptor.getValue()).contains("American");
    }

    @Test
    void 발신자_언어만_지정되면_반대_언어_문화권으로_번역한다() {
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(openAiTranslationClient.complete(promptCaptor.capture(), anyString())).thenReturn("안녕하세요");

        TranslationResponse response = translationService.translate(
                new TranslationRequest("Hello", Language.EN, null)
        );

        assertThat(response.getTranslated()).isEqualTo("안녕하세요");
        assertThat(promptCaptor.getValue()).contains("Korean workplace");
    }

    @Test
    void 언어_미지정_시_모델이_원문_언어를_스스로_판단하도록_한번만_호출한다() {
        when(openAiTranslationClient.complete(anyString(), any())).thenReturn("Hello");

        TranslationResponse response = translationService.translate(
                new TranslationRequest("안녕하세요", null, null)
        );

        assertThat(response.getTranslated()).isEqualTo("Hello");
        verify(openAiTranslationClient, times(1)).complete(anyString(), any());
    }

    @Test
    void 외부_API_호출이_실패하면_BusinessException으로_전파된다() {
        when(openAiTranslationClient.complete(anyString(), any()))
                .thenThrow(new BusinessException(ErrorCode.TRANSLATION_FAILED));

        assertThatThrownBy(() -> translationService.translate(new TranslationRequest("hi", Language.EN, Language.KR)))
                .isInstanceOf(BusinessException.class);
    }
}
