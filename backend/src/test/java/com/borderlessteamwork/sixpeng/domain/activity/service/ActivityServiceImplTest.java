package com.borderlessteamwork.sixpeng.domain.activity.service;

import com.borderlessteamwork.sixpeng.domain.activity.dto.response.ActivityResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingRepository;
import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.entity.DocumentSourceType;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** document/briefing 실제 리포지토리를 통해 활동 피드가 시간순으로 합쳐지는지 검증하는 도메인 간 통합 테스트. */
@SpringBootTest
@Transactional
class ActivityServiceImplTest {

    @Autowired
    ActivityService activityService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    BriefingRepository briefingRepository;

    Member owner;
    Member outsider;
    Project project;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(Member.ofGoogle("g-owner", "owner@example.com", "오너", Language.KR));
        outsider = memberRepository.save(Member.ofGoogle("g-out", "out@example.com", "외부인", Language.KR));
        project = projectRepository.save(Project.of("mine", owner));
        projectMemberRepository.save(ProjectMember.of(project, owner, "PM"));
    }

    @Test
    @DisplayName("문서 수집과 브리핑 생성 활동을 최신순으로 합쳐서 반환한다")
    void 활동을_최신순으로_합쳐서_반환한다() {
        Document oldDocument = documentRepository.save(
                Document.collect(project.getId(), DocumentSourceType.NOTION, "page-1", "오래된 문서", "내용", null));
        setCollectedAt(oldDocument, LocalDateTime.now().minusHours(2));

        Briefing briefing = briefingRepository.save(
                Briefing.builder().project(project).member(owner).summary("요약").build());
        setCreatedAt(briefing, LocalDateTime.now().minusHours(1));

        Document newDocument = documentRepository.save(
                Document.collect(project.getId(), DocumentSourceType.GOOGLE_MEET, "conf-1", "회의록", "내용", null));
        setCollectedAt(newDocument, LocalDateTime.now());

        List<ActivityResponse> activities = activityService.getRecentActivities(project.getId(), owner.getId());

        assertThat(activities).hasSize(3);
        assertThat(activities.get(0).type()).isEqualTo("DOCUMENT_COLLECTED");
        assertThat(activities.get(0).description()).contains("회의록");
        assertThat(activities.get(1).type()).isEqualTo("BRIEFING_GENERATED");
        assertThat(activities.get(2).description()).contains("오래된 문서");
    }

    @Test
    @DisplayName("참여자가 아니면 활동 피드를 볼 수 없다")
    void 참여자가_아니면_조회할_수_없다() {
        assertThatThrownBy(() -> activityService.getRecentActivities(project.getId(), outsider.getId()))
                .isInstanceOf(BusinessException.class);
    }

    private void setCollectedAt(Document document, LocalDateTime collectedAt) {
        org.springframework.test.util.ReflectionTestUtils.setField(document, "collectedAt", collectedAt);
        documentRepository.save(document);
    }

    private void setCreatedAt(Briefing briefing, LocalDateTime createdAt) {
        org.springframework.test.util.ReflectionTestUtils.setField(briefing, "createdAt", createdAt);
        briefingRepository.save(briefing);
    }
}
