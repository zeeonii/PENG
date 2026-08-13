package com.borderlessteamwork.sixpeng.domain.project.controller;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.support.TestLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private Member owner;
    private Member teammate;
    private Member outsider;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(Member.ofGoogle("g-owner", "owner@example.com", "오너", Language.KR));
        teammate = memberRepository.save(Member.ofGoogle("g-mate", "mate@example.com", "팀원", Language.EN));
        outsider = memberRepository.save(Member.ofGoogle("g-out", "out@example.com", "외부인", Language.EN));
    }

    private Project createProject(String name, Member creator) {
        Project project = projectRepository.save(Project.of(name, creator));
        projectMemberRepository.save(ProjectMember.of(project, creator, "PM"));
        return project;
    }

    @Test
    @DisplayName("프로젝트를 만들면 201 과 함께 생성자가 참여자로 등록된다")
    void createProject() throws Exception {
        mockMvc.perform(post("/projects").with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "sixpeng"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("sixpeng"))
                .andExpect(jsonPath("$.createdBy").value(owner.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        Project created = projectRepository.findAll().getFirst();
        assertThat(projectMemberRepository.existsByProjectIdAndMemberId(created.getId(), owner.getId())).isTrue();
    }

    @Test
    @DisplayName("CSRF 토큰이 없으면 로그인했어도 403 이다")
    void createProjectRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/projects").with(TestLogin.withoutCsrf(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "sixpeng"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("이름이 비어 있으면 400 이다")
    void createProjectRejectsBlankName() throws Exception {
        mockMvc.perform(post("/projects").with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("프로젝트 목록에는 내가 참여한 것만 나온다")
    void listOnlyMyProjects() throws Exception {
        createProject("mine", owner);
        createProject("someone-else", outsider);

        mockMvc.perform(get("/projects").with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("mine"));
    }

    @Test
    @DisplayName("참여하지 않은 프로젝트를 조회하면 403 이다")
    void getProjectDeniedForOutsider() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(get("/projects/{id}", project.getId()).with(TestLogin.as(outsider)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P002"));
    }

    @Test
    @DisplayName("없는 프로젝트는 404 다")
    void getProjectNotFound() throws Exception {
        mockMvc.perform(get("/projects/{id}", 9_999_999L).with(TestLogin.as(owner)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"));
    }

    @Test
    @DisplayName("이메일로 팀원을 초대하면 201 이고 멤버 목록에 나타난다")
    void inviteMember() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(post("/projects/{id}/members", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "mate@example.com", "role": "Backend"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/projects/{id}/members", project.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].memberId").value(teammate.getId()))
                .andExpect(jsonPath("$[1].name").value("팀원"))
                .andExpect(jsonPath("$[1].role").value("Backend"));
    }

    @Test
    @DisplayName("가입하지 않은 이메일로 초대하면 404 다")
    void inviteUnknownEmail() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(post("/projects/{id}/members", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nobody@example.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M001"));
    }

    @Test
    @DisplayName("이미 참여 중인 사람을 다시 초대하면 409 다")
    void inviteDuplicateMember() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(post("/projects/{id}/members", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "owner@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("P004"));
    }

    @Test
    @DisplayName("팀원을 내보내면 204 이고 목록에서 사라진다")
    void removeMember() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(delete("/projects/{id}/members/{memberId}", project.getId(), teammate.getId())
                        .with(TestLogin.as(owner)))
                .andExpect(status().isNoContent());

        assertThat(projectMemberRepository.existsByProjectIdAndMemberId(project.getId(), teammate.getId())).isFalse();
    }

    @Test
    @DisplayName("생성자는 내보낼 수 없다")
    void cannotRemoveOwner() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(delete("/projects/{id}/members/{memberId}", project.getId(), owner.getId())
                        .with(TestLogin.as(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P005"));
    }

    @Test
    @DisplayName("참여자가 아니면 초대할 수 없다")
    void outsiderCannotInvite() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(post("/projects/{id}/members", project.getId()).with(TestLogin.as(outsider))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "mate@example.com"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P002"));
    }
}
