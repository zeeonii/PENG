package com.borderlessteamwork.sixpeng.domain.project.controller;

import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectCreateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberInviteRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberRoleUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectNameUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectStatusUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.response.ProjectMemberResponse;
import com.borderlessteamwork.sixpeng.domain.project.dto.response.ProjectResponse;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;
import com.borderlessteamwork.sixpeng.domain.project.service.ProjectService;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * @param status 없으면 전체를 반환한다. 값이 enum 에 없으면 400 이다.
     */
    @GetMapping
    public List<ProjectResponse> getProjects(@CurrentMember Long memberId,
                                             @RequestParam(required = false) ProjectStatus status) {
        return projectService.findMyProjects(memberId, status).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@CurrentMember Long memberId,
                                         @Valid @RequestBody ProjectCreateRequest request) {
        return ProjectResponse.from(projectService.create(memberId, request));
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@CurrentMember Long memberId,
                                      @PathVariable Long projectId) {
        return ProjectResponse.from(projectService.findProject(projectId, memberId));
    }

    @PatchMapping("/{projectId}/status")
    public ProjectResponse updateProjectStatus(@CurrentMember Long memberId,
                                               @PathVariable Long projectId,
                                               @Valid @RequestBody ProjectStatusUpdateRequest request) {
        return ProjectResponse.from(projectService.updateStatus(projectId, memberId, request));
    }

    @PatchMapping("/{projectId}/name")
    public ProjectResponse updateProjectName(@CurrentMember Long memberId,
                                             @PathVariable Long projectId,
                                             @Valid @RequestBody ProjectNameUpdateRequest request) {
        return ProjectResponse.from(projectService.updateName(projectId, memberId, request));
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@CurrentMember Long memberId,
                              @PathVariable Long projectId) {
        projectService.delete(projectId, memberId);
    }

    @GetMapping("/{projectId}/members")
    public List<ProjectMemberResponse> getProjectMembers(@CurrentMember Long memberId,
                                                         @PathVariable Long projectId) {
        return projectService.findProjectMembers(projectId, memberId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteProjectMember(@CurrentMember Long memberId,
                                    @PathVariable Long projectId,
                                    @Valid @RequestBody ProjectMemberInviteRequest request) {
        projectService.invite(projectId, memberId, request);
    }

    @DeleteMapping("/{projectId}/members/{targetMemberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProjectMember(@CurrentMember Long memberId,
                                    @PathVariable Long projectId,
                                    @PathVariable Long targetMemberId) {
        projectService.removeMember(projectId, memberId, targetMemberId);
    }

    @PatchMapping("/{projectId}/members/{targetMemberId}/role")
    public ProjectMemberResponse updateProjectMemberRole(@CurrentMember Long memberId,
                                                          @PathVariable Long projectId,
                                                          @PathVariable Long targetMemberId,
                                                          @Valid @RequestBody ProjectMemberRoleUpdateRequest request) {
        return ProjectMemberResponse.from(projectService.updateMemberRole(projectId, memberId, targetMemberId, request));
    }
}
