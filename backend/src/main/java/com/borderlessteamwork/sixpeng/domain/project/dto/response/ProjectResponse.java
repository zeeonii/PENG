package com.borderlessteamwork.sixpeng.domain.project.dto.response;

import com.borderlessteamwork.sixpeng.domain.project.entity.Project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        Long createdBy,
        LocalDateTime createdAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getCreatedBy().getId(),
                project.getCreatedAt()
        );
    }
}
