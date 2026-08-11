package com.borderlessteamwork.sixpeng.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank
        @Size(max = 255)
        String name
) {
}
