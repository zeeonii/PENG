package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class TranslationServiceImpl implements TranslationService {

    private final GoogleTranslationClient googleTranslationClient;

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        String translatedText = translateText(
                request.getText(),
                request.getSenderLanguage(),
                request.getReceiverLanguage()
        );
        return TranslationResponse.of(request.getText(), translatedText);
    }

    private String translateText(String text, Language sender, Language receiver) {
        if (receiver != null) {
            String sourceCode = sender != null ? sender.getGoogleCode() : null;
            return googleTranslationClient.translate(text, receiver.getGoogleCode(), sourceCode).translatedText();
        }
        if (sender != null) {
            return googleTranslationClient.translate(text, sender.opposite().getGoogleCode(), sender.getGoogleCode())
                    .translatedText();
        }
        return translateAuto(text);
    }

    /**
     * 언어가 지정되지 않은 경우: 우선 영어로 번역을 시도해 원문 언어를 감지하고,
     * 감지된 언어가 한국어가 아니라면(=영어 등) 한국어로 다시 번역해 KR<->EN 자동 변환을 맞춘다.
     */
    private String translateAuto(String text) {
        GoogleTranslateApiResponse.Translation firstAttempt =
                googleTranslationClient.translate(text, Language.EN.getGoogleCode(), null);

        Language detected = Language.fromGoogleCode(firstAttempt.detectedSourceLanguage());
        if (detected == Language.KR) {
            return firstAttempt.translatedText();
        }

        return googleTranslationClient.translate(text, Language.KR.getGoogleCode(), firstAttempt.detectedSourceLanguage())
                .translatedText();
    }
}
