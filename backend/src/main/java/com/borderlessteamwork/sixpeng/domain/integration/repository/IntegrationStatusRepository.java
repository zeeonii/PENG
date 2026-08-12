package com.borderlessteamwork.sixpeng.domain.integration.repository;

import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegrationStatusRepository extends JpaRepository<IntegrationStatus, Long> {

    Optional<IntegrationStatus> findByProjectIdAndType(Long projectId, IntegrationType type);

    List<IntegrationStatus> findAllByProjectId(Long projectId);
}
