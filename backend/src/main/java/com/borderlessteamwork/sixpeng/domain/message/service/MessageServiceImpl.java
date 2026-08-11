package com.borderlessteamwork.sixpeng.domain.message.service;

import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import com.borderlessteamwork.sixpeng.domain.message.repository.MessageRepository;
import com.borderlessteamwork.sixpeng.domain.translation.dto.request.TranslationRequest;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final TranslationService translationService;

    @Override
    public List<MessageResponse> getMessages(Long memberId) {
        return messageRepository.findAllBySenderIdOrReceiverIdOrderByCreatedAtDesc(memberId, memberId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageCreateRequest request) {
        String translatedText = translationService.translate(
                new TranslationRequest(request.getOriginalText(), null, null)
        ).getTranslated();

        Message message = Message.create(
                request.getProjectId(),
                senderId,
                request.getReceiverId(),
                request.getOriginalText(),
                translatedText
        );
        return MessageResponse.from(messageRepository.save(message));
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        message.markAsRead();
    }
}
