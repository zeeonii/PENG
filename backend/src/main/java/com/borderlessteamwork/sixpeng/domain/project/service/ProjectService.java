package com.borderlessteamwork.sixpeng.domain.project.service;

import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingRepository;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingSourceRepository;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.integration.repository.IntegrationStatusRepository;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.message.repository.MessageRepository;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectCreateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberInviteRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberRoleUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectNameUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectStatusUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.domain.qna.repository.QnaHistoryRepository;
import com.borderlessteamwork.sixpeng.domain.qna.repository.QnaSourceRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    /** 프로젝트를 만든 사람에게 기본으로 부여하는 역할 */
    private static final String OWNER_ROLE = "PM";

    /** AI 팀원의 역할. 참여자 목록에서 사람 팀원과 구분하는 용도. */
    private static final String AI_ROLE = "AI";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final IntegrationStatusRepository integrationStatusRepository;
    private final MessageRepository messageRepository;
    private final BriefingRepository briefingRepository;
    private final BriefingSourceRepository briefingSourceRepository;
    private final QnaHistoryRepository qnaHistoryRepository;
    private final QnaSourceRepository qnaSourceRepository;

    @Transactional
    public Project create(Long memberId, ProjectCreateRequest request) {
        Member owner = findMember(memberId);
        Project project = projectRepository.save(Project.of(request.name(), owner));
        projectMemberRepository.save(ProjectMember.of(project, owner, OWNER_ROLE));
        // 요구사항 5-5: 사람 팀원과 함께 AI 팀원도 자동으로 합류한다.
        projectMemberRepository.save(ProjectMember.of(project, findAiTeammate(), AI_ROLE));
        return project;
    }

    /**
     * 내가 참여 중인 프로젝트만 보인다.
     *
     * @param status null 이면 상태와 무관하게 전체를 반환한다.
     */
    public List<Project> findMyProjects(Long memberId, ProjectStatus status) {
        if (status == null) {
            return projectMemberRepository.findProjectsByMemberId(memberId);
        }
        return projectMemberRepository.findProjectsByMemberIdAndStatus(memberId, status);
    }

    /** 상태 변경은 참여자면 누구나 할 수 있다. invite/removeMember 와 같은 규칙이다. */
    @Transactional
    public Project updateStatus(Long projectId, Long memberId, ProjectStatusUpdateRequest request) {
        Project project = findProjectById(projectId);
        validateParticipant(projectId, memberId);

        project.updateStatus(request.status());
        return project;
    }

    /** 이름 변경은 참여자면 누구나 할 수 있다. 상태 변경과 같은 규칙이다. */
    @Transactional
    public Project updateName(Long projectId, Long memberId, ProjectNameUpdateRequest request) {
        Project project = findProjectById(projectId);
        validateParticipant(projectId, memberId);

        project.updateName(request.name());
        return project;
    }

    /**
     * 프로젝트 삭제는 생성자만 할 수 있다. 되돌릴 수 없고 팀원 전체의 데이터(문서, 브리핑,
     * QnA 기록 등)에 영향을 주는 파괴적인 작업이라 참여자 전체가 아닌 소유자로 제한한다.
     *
     * <p>FK 제약이 있는 자식 테이블(qna_source, briefing_source, qna_history, briefing,
     * project_member)을 먼저 지우고, FK는 없지만 project_id로 정리해야 하는 나머지
     * (document, integration_status, message)를 지운 뒤 마지막에 project를 지운다.
     */
    @Transactional
    public void delete(Long projectId, Long memberId) {
        Project project = findProjectById(projectId);
        if (!project.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_DELETE_FORBIDDEN);
        }

        qnaSourceRepository.deleteByQnaHistoryProjectId(projectId);
        qnaHistoryRepository.deleteByProjectId(projectId);
        briefingSourceRepository.deleteByBriefingProjectId(projectId);
        briefingRepository.deleteByProjectId(projectId);
        documentRepository.deleteByProjectId(projectId);
        integrationStatusRepository.deleteByProjectId(projectId);
        messageRepository.deleteByProjectId(projectId);
        projectMemberRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    /**
     * 팀원 역할 변경은 참여자면 누구나 할 수 있다. invite/removeMember 와 같은 규칙이다.
     * AI 팀원의 역할은 화면에서 사람 팀원과 구분하는 고정값이라 바꿀 수 없다.
     */
    @Transactional
    public ProjectMember updateMemberRole(Long projectId, Long memberId, Long targetMemberId,
                                          ProjectMemberRoleUpdateRequest request) {
        findProjectById(projectId);
        validateParticipant(projectId, memberId);

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndMemberId(projectId, targetMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        if (projectMember.getMember().isAiTeammate()) {
            throw new BusinessException(ErrorCode.PROJECT_AI_TEAMMATE_ROLE_CANNOT_BE_CHANGED);
        }

        projectMember.updateRole(request.role());
        return projectMember;
    }

    public Project findProject(Long projectId, Long memberId) {
        Project project = findProjectById(projectId);
        validateParticipant(projectId, memberId);
        return project;
    }

    public List<ProjectMember> findProjectMembers(Long projectId, Long memberId) {
        findProjectById(projectId);
        validateParticipant(projectId, memberId);
        return projectMemberRepository.findAllByProjectIdWithMember(projectId);
    }

    @Transactional
    public void invite(Long projectId, Long memberId, ProjectMemberInviteRequest request) {
        Project project = findProjectById(projectId);
        validateParticipant(projectId, memberId);

        Member invitee = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (projectMemberRepository.existsByProjectIdAndMemberId(projectId, invitee.getId())) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        }

        projectMemberRepository.save(ProjectMember.of(project, invitee, request.role()));
    }

    @Transactional
    public void removeMember(Long projectId, Long requesterId, Long targetMemberId) {
        Project project = findProjectById(projectId);
        validateParticipant(projectId, requesterId);

        // 생성자를 빼면 project.created_by 가 참여자 목록에 없는 상태가 되므로 막는다.
        if (project.isOwnedBy(targetMemberId)) {
            throw new BusinessException(ErrorCode.PROJECT_OWNER_CANNOT_BE_REMOVED);
        }

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndMemberId(projectId, targetMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        // AI 팀원은 모든 프로젝트에 항상 참여해 있어야 하므로 내보낼 수 없다.
        if (projectMember.getMember().isAiTeammate()) {
            throw new BusinessException(ErrorCode.PROJECT_AI_TEAMMATE_CANNOT_BE_REMOVED);
        }

        projectMemberRepository.delete(projectMember);
    }

    private Member findAiTeammate() {
        return memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_TEAMMATE_NOT_INITIALIZED));
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateParticipant(Long projectId, Long memberId) {
        if (!projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
