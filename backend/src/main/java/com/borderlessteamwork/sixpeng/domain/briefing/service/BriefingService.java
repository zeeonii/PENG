package com.borderlessteamwork.sixpeng.domain.briefing.service;

import com.borderlessteamwork.sixpeng.domain.briefing.dto.response.BriefingDetailResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.dto.response.BriefingSummaryResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.BriefingSource;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingRepository;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingSourceRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private final BriefingRepository briefingRepository;
    private final BriefingSourceRepository briefingSourceRepository;
    private final BriefingGenerationService briefingGenerationService;

    @Transactional
    public List<BriefingSummaryResponse> getTodayBriefings(Long projectId, Long memberId) {
        briefingGenerationService.generateTodayBriefingIfNeeded(projectId, memberId);

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        return briefingRepository.findByProjectIdAndMemberIdOrderByCreatedAtDesc(projectId, memberId)
                .stream()
                .filter(briefing -> !briefing.getCreatedAt().isBefore(startOfToday))
                .map(BriefingSummaryResponse::from)
                .toList();
    }

    public BriefingDetailResponse getBriefingDetail(Long briefingId) {
        Briefing briefing = briefingRepository.findById(briefingId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Briefing not found: id=" + briefingId));

        List<BriefingSource> sources = briefingSourceRepository.findByBriefingId(briefingId);

        return BriefingDetailResponse.of(briefing, sources);
    }
}
