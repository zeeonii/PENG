package com.borderlessteamwork.sixpeng.domain.integration.dto.response;

import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationConnectionStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IntegrationStatusResponse {

    private final IntegrationType type;
    private final IntegrationConnectionStatus status;
    private final LocalDateTime lastSyncedAt;

    private IntegrationStatusResponse(IntegrationStatus integrationStatus) {
        this.type = integrationStatus.getType();
        this.status = integrationStatus.getStatus();
        this.lastSyncedAt = integrationStatus.getLastSyncedAt();
    }

    public static IntegrationStatusResponse from(IntegrationStatus integrationStatus) {
        return new IntegrationStatusResponse(integrationStatus);
    }
}
