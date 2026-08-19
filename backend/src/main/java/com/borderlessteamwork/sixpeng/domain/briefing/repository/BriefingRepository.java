package com.borderlessteamwork.sixpeng.domain.briefing.repository;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BriefingRepository extends JpaRepository<Briefing, Long> {

    List<Briefing> findByProjectIdAndMemberIdOrderByCreatedAtDesc(Long projectId, Long memberId);

    Optional<Briefing> findFirstByProjectIdAndMemberIdOrderByCreatedAtDesc(Long projectId, Long memberId);
}
