package com.borderlessteamwork.sixpeng.domain.briefing.repository;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.BriefingSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingSourceRepository extends JpaRepository<BriefingSource, Long> {

    List<BriefingSource> findByBriefingId(Long briefingId);
}
