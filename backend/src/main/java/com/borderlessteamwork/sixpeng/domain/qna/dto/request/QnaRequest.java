package com.borderlessteamwork.sixpeng.domain.qna.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QnaRequest(
        @NotNull Long memberId,
        @NotBlank String question
) {}
