package com.borderlessteamwork.sixpeng.domain.translation.dto.request;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {

    @NotBlank
    private String text;

    private Language senderLanguage;

    private Language receiverLanguage;
}
