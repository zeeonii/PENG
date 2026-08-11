package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 단순 직역이 아니라, 대상 문화권의 업무 커뮤니케이션 뉘앙스(직설/격식 등)까지 반영하는
 * Cultural Translation을 수행한다. 언어를 지정하지 않으면 모델이 원문 언어를 스스로 판단한다.
 */
@Service
@RequiredArgsConstructor
class TranslationServiceImpl implements TranslationService {

    private final OpenAiTranslationClient openAiTranslationClient;

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        String systemPrompt = buildSystemPrompt(request.getSenderLanguage(), request.getReceiverLanguage());
        String translatedText = openAiTranslationClient.complete(systemPrompt, request.getText());
        return TranslationResponse.of(request.getText(), translatedText);
    }

    private String buildSystemPrompt(Language sender, Language receiver) {
        if (receiver != null) {
            return culturalTranslationPrompt(sender, receiver);
        }
        if (sender != null) {
            return culturalTranslationPrompt(sender, sender.opposite());
        }
        return autoDetectPrompt();
    }

    private String culturalTranslationPrompt(Language from, Language to) {
        String fromLabel = from != null ? languageName(from) : "Korean or English (detect automatically)";
        return """
                You are a professional translator specializing in cross-cultural business \
                communication between Korea and the United States.
                Translate the user's message from %s to %s. Beyond literal translation, adapt \
                tone, directness, and phrasing to fit typical %s workplace communication norms, \
                while preserving the original meaning.
                Reply with ONLY the translated text — no explanation, no quotes, no language labels.\
                """.formatted(fromLabel, languageName(to), cultureName(to));
    }

    private String autoDetectPrompt() {
        return """
                You are a professional translator specializing in cross-cultural business \
                communication between Korea and the United States.
                Detect whether the user's message is written in Korean or English.
                - If Korean, translate it into English, adapting tone and phrasing to fit typical \
                American workplace communication norms (more direct and concise).
                - If English, translate it into Korean, adapting tone and phrasing to fit typical \
                Korean workplace communication norms (appropriately polite and formal).
                Preserve the original meaning.
                Reply with ONLY the translated text — no explanation, no quotes, no language labels.\
                """;
    }

    private String languageName(Language language) {
        return language == Language.KR ? "Korean" : "English";
    }

    private String cultureName(Language language) {
        return language == Language.KR ? "Korean" : "American";
    }
}
