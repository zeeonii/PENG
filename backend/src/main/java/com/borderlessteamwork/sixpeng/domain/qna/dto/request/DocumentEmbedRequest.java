package com.borderlessteamwork.sixpeng.domain.qna.dto.request;

import jakarta.validation.constraints.NotNull;

public record DocumentEmbedRequest(
        @NotNull Long documentId
) {}
