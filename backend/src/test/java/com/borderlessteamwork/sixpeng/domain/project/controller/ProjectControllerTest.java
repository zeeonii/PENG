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
import static org.hamcrest.Matchers.hasSize;
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
    private Member aiTeammate;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(Member.ofGoogle("g-owner", "owner@example.com", "오너", Language.KR));
        teammate = memberRepository.save(Member.ofGoogle("g-mate", "mate@example.com", "팀원", Language.EN));
        outsider = memberRepository.save(Member.ofGoogle("g-out", "out@example.com", "외부인", Language.EN));
        // 시딩은 컨텍스트 시작 시 한 번 일어나는데 컨텍스트가 여러 개라 create-drop 순서에 따라
        // 사라질 수 있다. 테스트가 그 순서에 기대지 않도록 여기서 확보한다.
        aiTeammate = memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .orElseGet(() -> memberRepository.save(Member.ofAiTeammate()));
    }

    /** POST /projects 와 같은 모양(생성자 + AI 팀원)으로 프로젝트를 만든다. */
    private Project createProject(String name, Member creator) {
        Project project = projectRepository.save(Project.of(name, creator));
        projectMemberRepository.save(ProjectMember.of(project, creator, "PM"));
        projectMemberRepository.save(ProjectMember.of(project, aiTeammate, "AI"));
        return project;
    }

    @Test
    @DisplayName("프로젝트를 만들면 생성자와 AI 팀원이 함께 참여자로 등록된다")
    void createProjectAutoJoinsOwnerAndAiTeammate() throws Exception {
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
        assertThat(projectMemberRepository.existsByProjectIdAndMemberId(created.getId(), aiTeammate.getId())).isTrue();

        mockMvc.perform(get("/projects/{id}/members", created.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$[0].role").value("PM"))
                .andExpect(jsonPath("$[0].isAiTeammate").value(false))
                .andExpect(jsonPath("$[1].memberId").value(aiTeammate.getId()))
                .andExpect(jsonPath("$[1].name").value("AI 팀원"))
                .andExpect(jsonPath("$[1].role").value("AI"))
                .andExpect(jsonPath("$[1].isAiTeammate").value(true));
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

        // 생성자 + AI 팀원 + 초대된 팀원
        mockMvc.perform(get("/projects/{id}/members", project.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[2].memberId").value(teammate.getId()))
                .andExpect(jsonPath("$[2].name").value("팀원"))
                .andExpect(jsonPath("$[2].role").value("Backend"))
                .andExpect(jsonPath("$[2].isAiTeammate").value(false));
    }

    @Test
    @DisplayName("참여자 목록은 AI 팀원만 isAiTeammate=true 로 내려준다")
    void memberListMarksOnlyAiTeammate() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        // 프론트는 id 하드코딩 없이 이 플래그만으로 AI 팀원을 구분할 수 있어야 한다.
        mockMvc.perform(get("/projects/{id}/members", project.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.isAiTeammate == true)]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.isAiTeammate == false)]", hasSize(2)))
                .andExpect(jsonPath("$[1].memberId").value(aiTeammate.getId()))
                .andExpect(jsonPath("$[1].isAiTeammate").value(true))
                .andExpect(jsonPath("$[0].isAiTeammate").value(false))
                .andExpect(jsonPath("$[2].isAiTeammate").value(false));
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
    @DisplayName("AI 팀원은 내보낼 수 없다")
    void cannotRemoveAiTeammate() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(delete("/projects/{id}/members/{memberId}", project.getId(), aiTeammate.getId())
                        .with(TestLogin.as(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P006"));

        assertThat(projectMemberRepository.existsByProjectIdAndMemberId(project.getId(), aiTeammate.getId())).isTrue();
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
