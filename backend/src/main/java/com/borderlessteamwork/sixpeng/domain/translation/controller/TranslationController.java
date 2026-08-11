package com.borderlessteamwork.sixpeng.domain.translation.controller;

import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<TranslationResponse> translate(@Valid @RequestBody TranslationRequest request) {
        return ResponseEntity.ok(translationService.translate(request));
    }
}
