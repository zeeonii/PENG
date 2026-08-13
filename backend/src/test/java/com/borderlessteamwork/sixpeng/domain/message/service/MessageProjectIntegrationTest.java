package com.borderlessteamwork.sixpeng.domain.message.service;

import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.entity.ProjectMember;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MessageService가 목(mock)이 아니라 실제 ProjectMemberRepository/Project/Member 엔티티를 통해
 * "받는 사람이 프로젝트 참여자인지"를 올바르게 검증하는지 확인하는 도메인 간 통합 테스트.
 * TranslationService만 목으로 대체해 외부 API 호출을 막는다.
 */
@SpringBootTest
@Transactional
class MessageProjectIntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    MessageService messageService;

    @MockitoBean
    TranslationService translationService;

    private Member sender;
    private Member receiver;
    private Project project;

    @BeforeEach
    void setUp() {
        sender = memberRepository.save(Member.ofGoogle("g-sender", "sender@example.com", "보낸사람", Language.KR));
        receiver = memberRepository.save(Member.ofGoogle("g-receiver", "receiver@example.com", "받는사람", Language.EN));
        project = projectRepository.save(Project.of("real project", sender));
        projectMemberRepository.save(ProjectMember.of(project, sender, "PM"));
        projectMemberRepository.save(ProjectMember.of(project, receiver, "Backend"));

        when(translationService.translate(any())).thenReturn(TranslationResponse.of("hi", "안녕"));
    }

    @Test
    @DisplayName("실제 프로젝트 참여자에게는 쪽지를 보낼 수 있다")
    void 참여자에게_쪽지를_보낼_수_있다() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(project.getId());
        request.setReceiverId(receiver.getId());
        request.setOriginalText("hi");

        MessageResponse response = messageService.sendMessage(sender.getId(), request);

        assertThat(response.getSenderId()).isEqualTo(sender.getId());
        assertThat(response.getReceiverId()).isEqualTo(receiver.getId());
        assertThat(response.getTranslatedText()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("받는 사람이 실제로 프로젝트를 나가면 그 이후로는 쪽지를 보낼 수 없다")
    void 프로젝트를_나간_실제_참여자에게는_쪽지를_보낼_수_없다() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(project.getId());
        request.setReceiverId(receiver.getId());
        request.setOriginalText("hi");

        ProjectMember receiverMembership = projectMemberRepository
                .findByProjectIdAndMemberId(project.getId(), receiver.getId())
                .orElseThrow();
        projectMemberRepository.delete(receiverMembership);
        projectMemberRepository.flush();

        assertThatThrownBy(() -> messageService.sendMessage(sender.getId(), request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }
}
