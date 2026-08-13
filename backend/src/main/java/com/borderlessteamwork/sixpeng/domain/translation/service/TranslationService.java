package com.borderlessteamwork.sixpeng.domain.translation.service;

import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;

public interface TranslationService {

    TranslationResponse translate(TranslationRequest request);
}
