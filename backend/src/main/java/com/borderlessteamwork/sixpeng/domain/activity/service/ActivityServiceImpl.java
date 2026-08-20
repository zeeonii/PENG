package com.borderlessteamwork.sixpeng.domain.activity.service;

import com.borderlessteamwork.sixpeng.domain.activity.dto.response.ActivityResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingRepository;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ActivityServiceImpl implements ActivityService {

    private static final int LIMIT = 10;

    private final DocumentRepository documentRepository;
    private final BriefingRepository briefingRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public List<ActivityResponse> getRecentActivities(Long projectId, Long memberId) {
        validateParticipant(projectId, memberId);

        List<ActivityResponse> activities = new ArrayList<>();
        documentRepository.findTop10ByProjectIdOrderByCollectedAtDesc(projectId)
                .forEach(document -> activities.add(ActivityResponse.fromDocument(document)));
        briefingRepository.findTop10ByProjectIdAndHasUpdateTrueOrderByCreatedAtDesc(projectId)
                .forEach(briefing -> activities.add(ActivityResponse.fromBriefing(briefing)));

        return activities.stream()
                .sorted(Comparator.comparing(ActivityResponse::occurredAt).reversed())
                .limit(LIMIT)
                .toList();
    }

    private void validateParticipant(Long projectId, Long memberId) {
        if (!projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
