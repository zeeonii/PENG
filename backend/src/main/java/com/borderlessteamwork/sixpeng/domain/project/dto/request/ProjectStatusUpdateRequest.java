package com.borderlessteamwork.sixpeng.domain.project.dto.request;

import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(
        @NotNull
        ProjectStatus status
) {
}
