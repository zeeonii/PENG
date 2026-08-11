package com.borderlessteamwork.sixpeng.domain.project.service;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectCreateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberInviteRequest;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
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

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Project create(Long memberId, ProjectCreateRequest request) {
        Member owner = findMember(memberId);
        Project project = projectRepository.save(Project.of(request.name(), owner));
        projectMemberRepository.save(ProjectMember.of(project, owner, OWNER_ROLE));
        return project;
    }

    /** 내가 참여 중인 프로젝트만 보인다. */
    public List<Project> findMyProjects(Long memberId) {
        return projectMemberRepository.findProjectsByMemberId(memberId);
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
        projectMemberRepository.delete(projectMember);
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
