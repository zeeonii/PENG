package com.borderlessteamwork.sixpeng.domain.message.service;

import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import com.borderlessteamwork.sixpeng.domain.message.repository.MessageRepository;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectMemberRepository;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    TranslationService translationService;

    @Mock
    ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    MessageServiceImpl messageService;

    @Test
    void 내가_보냈거나_받은_쪽지_목록을_조회한다() {
        Message message = Message.create(1L, 10L, 20L, "hi", "안녕");
        when(messageRepository.findAllBySenderIdOrReceiverIdOrderByCreatedAtDesc(10L, 10L))
                .thenReturn(List.of(message));

        List<MessageResponse> responses = messageService.getMessages(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOriginalText()).isEqualTo("hi");
    }

    @Test
    void 쪽지_전송시_번역서비스를_호출해서_번역문을_같이_저장한다() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("안녕하세요");

        when(projectMemberRepository.existsByProjectIdAndMemberId(1L, 20L)).thenReturn(true);
        when(translationService.translate(any())).thenReturn(TranslationResponse.of("안녕하세요", "Hello"));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = messageService.sendMessage(10L, request);

        assertThat(response.getSenderId()).isEqualTo(10L);
        assertThat(response.getReceiverId()).isEqualTo(20L);
        assertThat(response.getOriginalText()).isEqualTo("안녕하세요");
        assertThat(response.getTranslatedText()).isEqualTo("Hello");
        assertThat(response.isRead()).isFalse();
    }

    @Test
    void 받는_사람이_프로젝트를_나갔으면_쪽지를_보낼_수_없다() {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("안녕하세요");

        when(projectMemberRepository.existsByProjectIdAndMemberId(1L, 20L)).thenReturn(false);

        assertThatThrownBy(() -> messageService.sendMessage(10L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        verify(messageRepository, never()).save(any());
        verify(translationService, never()).translate(any());
    }

    @Test
    void 존재하는_쪽지를_읽음_처리한다() {
        Message message = Message.create(1L, 10L, 20L, "hi", "안녕");
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.markAsRead(1L);

        assertThat(message.isRead()).isTrue();
    }

    @Test
    void 존재하지_않는_쪽지를_읽음_처리하면_예외가_발생한다() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.markAsRead(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND));

        verify(messageRepository, never()).save(any());
    }
}
