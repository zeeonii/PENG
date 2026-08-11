package com.borderlessteamwork.sixpeng.domain.message.service;

import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {

    List<MessageResponse> getMessages(Long memberId);

    MessageResponse sendMessage(Long senderId, MessageCreateRequest request);

    void markAsRead(Long messageId);
}
