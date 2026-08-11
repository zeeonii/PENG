package com.borderlessteamwork.sixpeng.domain.message.dto.response;

import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {

    private final Long id;
    private final Long projectId;
    private final Long senderId;
    private final Long receiverId;
    private final String originalText;
    private final String translatedText;

    @JsonProperty("isRead")
    private final boolean isRead;

    private final LocalDateTime createdAt;

    private MessageResponse(Message message) {
        this.id = message.getId();
        this.projectId = message.getProjectId();
        this.senderId = message.getSenderId();
        this.receiverId = message.getReceiverId();
        this.originalText = message.getOriginalText();
        this.translatedText = message.getTranslatedText();
        this.isRead = message.isRead();
        this.createdAt = message.getCreatedAt();
    }

    public static MessageResponse from(Message message) {
        return new MessageResponse(message);
    }
}
