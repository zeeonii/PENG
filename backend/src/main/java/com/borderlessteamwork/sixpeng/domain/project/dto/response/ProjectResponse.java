package com.borderlessteamwork.sixpeng.domain.project.dto.response;

import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        ProjectStatus status,
        Long createdBy,
        LocalDateTime createdAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedAt()
        );
    }
}
