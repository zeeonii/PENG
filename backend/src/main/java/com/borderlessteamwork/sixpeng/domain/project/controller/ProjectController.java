package com.borderlessteamwork.sixpeng.domain.project.controller;

import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectCreateRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.request.ProjectMemberInviteRequest;
import com.borderlessteamwork.sixpeng.domain.project.dto.response.ProjectMemberResponse;
import com.borderlessteamwork.sixpeng.domain.project.dto.response.ProjectResponse;
import com.borderlessteamwork.sixpeng.domain.project.service.ProjectService;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> getProjects(@CurrentMember Long memberId) {
        return projectService.findMyProjects(memberId).stream()
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
}
