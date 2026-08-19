package com.borderlessteamwork.sixpeng.domain.qna.dto.request;

import jakarta.validation.constraints.NotBlank;

public record QnaRequest(
        @NotBlank String question
) {}
