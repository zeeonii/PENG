package com.borderlessteamwork.sixpeng.domain.briefing.repository;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BriefingRepository extends JpaRepository<Briefing, Long> {

    List<Briefing> findByProjectIdAndMemberIdOrderByCreatedAtDesc(Long projectId, Long memberId);

    /** hasUpdate 여부와 무관하게 가장 최근 row를 찾는다 — "마지막으로 검토한 시점" 커서용. */
    Optional<Briefing> findFirstByProjectIdAndMemberIdOrderByCreatedAtDesc(Long projectId, Long memberId);

    /** 프로젝트 홈의 "최근 활동" 피드용. 특정 회원이 아니라 프로젝트 전체 기준이다. */
    List<Briefing> findTop10ByProjectIdAndHasUpdateTrueOrderByCreatedAtDesc(Long projectId);
}
