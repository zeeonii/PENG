package com.borderlessteamwork.sixpeng.domain.project.controller;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @DisplayName("로그인하지 않으면 프로젝트를 만들 수 없다")
    void createProjectRequiresLogin() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "sixpeng"}
                                """))
                .andExpect(status().isUnauthorized());
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
    @DisplayName("참여자 목록에 담당 업무가 포함된다")
    void memberListIncludesDuty() throws Exception {
        Project project = createProject("mine", owner);
        owner.updateProfile(null, null, null, "백엔드 개발", owner.isShowOriginalText(), owner.isShowTranslatedText());
        memberRepository.save(owner);

        mockMvc.perform(get("/projects/{id}/members", project.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$[0].duty").value("백엔드 개발"));
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

    @Test
    @DisplayName("새로 만든 프로젝트는 PENDING 상태다")
    void createdProjectStartsPending() throws Exception {
        mockMvc.perform(post("/projects").with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "sixpeng"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(projectRepository.findAll().getFirst().getStatus()).isEqualTo(ProjectStatus.PENDING);
    }

    @Test
    @DisplayName("status 파라미터가 없으면 상태와 무관하게 전체가 나온다")
    void listWithoutStatusReturnsAll() throws Exception {
        createProject("pending", owner);
        createProject("running", owner).updateStatus(ProjectStatus.IN_PROGRESS);
        createProject("done", owner).updateStatus(ProjectStatus.COMPLETED);

        mockMvc.perform(get("/projects").with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("status 로 목록을 거를 수 있다")
    void listFilteredByStatus() throws Exception {
        createProject("pending", owner);
        createProject("running", owner).updateStatus(ProjectStatus.IN_PROGRESS);
        createProject("done", owner).updateStatus(ProjectStatus.COMPLETED);

        mockMvc.perform(get("/projects").param("status", "IN_PROGRESS").with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("running"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        mockMvc.perform(get("/projects").param("status", "PENDING").with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("pending"));
    }

    @Test
    @DisplayName("상태로 걸러도 내가 참여한 프로젝트만 나온다")
    void listFilteredByStatusStillScopedToMe() throws Exception {
        createProject("mine", owner).updateStatus(ProjectStatus.IN_PROGRESS);
        createProject("someone-else", outsider).updateStatus(ProjectStatus.IN_PROGRESS);

        mockMvc.perform(get("/projects").param("status", "IN_PROGRESS").with(TestLogin.as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("mine"));
    }

    @Test
    @DisplayName("enum 에 없는 status 값은 500 이 아니라 400 이다")
    void listRejectsUnknownStatus() throws Exception {
        // 프론트가 한동안 한글 라벨("진행중")로 필터링하고 있었어서 실제로 밟기 쉬운 경로다.
        mockMvc.perform(get("/projects").param("status", "진행중").with(TestLogin.as(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("상태를 변경하면 200 이고 바뀐 값이 내려온다")
    void updateStatus() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/status", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "IN_PROGRESS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/projects/{id}", project.getId()).with(TestLogin.as(owner)))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("생성자가 아닌 참여자도 상태를 바꿀 수 있다")
    void participantCanUpdateStatus() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/status", project.getId()).with(TestLogin.as(teammate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("참여자가 아니면 상태를 바꿀 수 없다")
    void outsiderCannotUpdateStatus() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/status", project.getId()).with(TestLogin.as(outsider))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P002"));

        assertThat(projectRepository.findById(project.getId()).orElseThrow().getStatus())
                .isEqualTo(ProjectStatus.PENDING);
    }

    @Test
    @DisplayName("없는 프로젝트의 상태를 바꾸면 404 다")
    void updateStatusNotFound() throws Exception {
        mockMvc.perform(patch("/projects/{id}/status", 9_999_999L).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"));
    }

    @Test
    @DisplayName("status 가 없거나 enum 에 없는 값이면 400 이다")
    void updateStatusRejectsInvalidBody() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/status", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));

        mockMvc.perform(patch("/projects/{id}/status", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "ARCHIVED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("로그인하지 않으면 상태를 변경할 수 없다")
    void updateStatusRequiresLogin() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/status", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "IN_PROGRESS"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이름을 변경하면 200이고 바뀐 값이 내려온다")
    void updateName() throws Exception {
        Project project = createProject("old-name", owner);

        mockMvc.perform(patch("/projects/{id}/name", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "new-name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("new-name"));

        assertThat(projectRepository.findById(project.getId()).orElseThrow().getName()).isEqualTo("new-name");
    }

    @Test
    @DisplayName("생성자가 아닌 참여자도 이름을 바꿀 수 있다")
    void participantCanUpdateName() throws Exception {
        Project project = createProject("old-name", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/name", project.getId()).with(TestLogin.as(teammate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "new-name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new-name"));
    }

    @Test
    @DisplayName("참여자가 아니면 이름을 바꿀 수 없다")
    void outsiderCannotUpdateName() throws Exception {
        Project project = createProject("old-name", owner);

        mockMvc.perform(patch("/projects/{id}/name", project.getId()).with(TestLogin.as(outsider))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "new-name"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P002"));
    }

    @Test
    @DisplayName("이름이 비어 있으면 400이다")
    void updateNameRejectsBlank() throws Exception {
        Project project = createProject("old-name", owner);

        mockMvc.perform(patch("/projects/{id}/name", project.getId()).with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("로그인하지 않으면 이름을 변경할 수 없다")
    void updateNameRequiresLogin() throws Exception {
        Project project = createProject("old-name", owner);

        mockMvc.perform(patch("/projects/{id}/name", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "new-name"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("생성자가 프로젝트를 삭제하면 204이고 목록에서 사라진다")
    void deleteProject() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(delete("/projects/{id}", project.getId()).with(TestLogin.as(owner)))
                .andExpect(status().isNoContent());

        assertThat(projectRepository.findById(project.getId())).isEmpty();
        assertThat(projectMemberRepository.existsByProjectIdAndMemberId(project.getId(), owner.getId())).isFalse();
    }

    @Test
    @DisplayName("생성자가 아니면 프로젝트를 삭제할 수 없다")
    void nonOwnerCannotDeleteProject() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(delete("/projects/{id}", project.getId()).with(TestLogin.as(teammate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P007"));

        assertThat(projectRepository.findById(project.getId())).isPresent();
    }

    @Test
    @DisplayName("참여자가 아니면 프로젝트를 삭제할 수 없다")
    void outsiderCannotDeleteProject() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(delete("/projects/{id}", project.getId()).with(TestLogin.as(outsider)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P007"));
    }

    @Test
    @DisplayName("없는 프로젝트를 삭제하면 404다")
    void deleteProjectNotFound() throws Exception {
        mockMvc.perform(delete("/projects/{id}", 9_999_999L).with(TestLogin.as(owner)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P001"));
    }

    @Test
    @DisplayName("로그인하지 않으면 프로젝트를 삭제할 수 없다")
    void deleteProjectRequiresLogin() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(delete("/projects/{id}", project.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("팀원 역할을 변경하면 200이고 바뀐 값이 내려온다")
    void updateMemberRole() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), teammate.getId())
                        .with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Frontend"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(teammate.getId()))
                .andExpect(jsonPath("$.role").value("Frontend"));
    }

    @Test
    @DisplayName("참여자가 아니면 팀원 역할을 바꿀 수 없다")
    void outsiderCannotUpdateMemberRole() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), teammate.getId())
                        .with(TestLogin.as(outsider))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Frontend"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("P002"));
    }

    @Test
    @DisplayName("AI 팀원의 역할은 바꿀 수 없다")
    void cannotUpdateAiTeammateRole() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), aiTeammate.getId())
                        .with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Frontend"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("P008"));
    }

    @Test
    @DisplayName("없는 팀원의 역할을 바꾸면 404다")
    void updateMemberRoleNotFound() throws Exception {
        Project project = createProject("mine", owner);

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), teammate.getId())
                        .with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Frontend"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("P003"));
    }

    @Test
    @DisplayName("역할이 비어 있으면 400이다")
    void updateMemberRoleRejectsBlank() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), teammate.getId())
                        .with(TestLogin.as(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("로그인하지 않으면 팀원 역할을 바꿀 수 없다")
    void updateMemberRoleRequiresLogin() throws Exception {
        Project project = createProject("mine", owner);
        projectMemberRepository.save(ProjectMember.of(project, teammate, "Backend"));

        mockMvc.perform(patch("/projects/{id}/members/{memberId}/role", project.getId(), teammate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "Frontend"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
