package com.borderlessteamwork.sixpeng.domain.integration.repository;

import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationConnectionStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationStatus;
import com.borderlessteamwork.sixpeng.domain.integration.entity.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegrationStatusRepository extends JpaRepository<IntegrationStatus, Long> {

    Optional<IntegrationStatus> findByProjectIdAndType(Long projectId, IntegrationType type);

    List<IntegrationStatus> findAllByProjectId(Long projectId);

    /** 프로젝트 삭제 시 연동 상태 행을 정리하는 용도. */
    void deleteByProjectId(Long projectId);

    /** 주기적 재동기화(폴링) 대상을 찾는 용도. */
    List<IntegrationStatus> findAllByTypeAndStatus(IntegrationType type, IntegrationConnectionStatus status);
}
