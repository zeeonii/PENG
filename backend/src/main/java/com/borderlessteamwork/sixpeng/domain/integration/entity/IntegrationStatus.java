package com.borderlessteamwork.sixpeng.domain.integration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "integration_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "type"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntegrationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationConnectionStatus status;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "workspace_name")
    private String workspaceName;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    private IntegrationStatus(Long projectId, IntegrationType type) {
        this.projectId = projectId;
        this.type = type;
        this.status = IntegrationConnectionStatus.DISCONNECTED;
    }

    public static IntegrationStatus connectNotion(Long projectId, String accessToken, String workspaceId, String workspaceName) {
        IntegrationStatus integrationStatus = new IntegrationStatus(projectId, IntegrationType.NOTION);
        integrationStatus.updateNotionConnection(accessToken, workspaceId, workspaceName);
        return integrationStatus;
    }

    public void updateNotionConnection(String accessToken, String workspaceId, String workspaceName) {
        this.status = IntegrationConnectionStatus.CONNECTED;
        this.accessToken = accessToken;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.lastSyncedAt = LocalDateTime.now();
    }

    public static IntegrationStatus connectGoogleMeet(Long projectId, String accessToken) {
        IntegrationStatus integrationStatus = new IntegrationStatus(projectId, IntegrationType.GOOGLE_MEET);
        integrationStatus.updateGoogleMeetConnection(accessToken);
        return integrationStatus;
    }

    public void updateGoogleMeetConnection(String accessToken) {
        this.status = IntegrationConnectionStatus.CONNECTED;
        this.accessToken = accessToken;
        this.lastSyncedAt = LocalDateTime.now();
    }
}
