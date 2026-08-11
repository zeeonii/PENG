package com.borderlessteamwork.sixpeng.domain.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Lob
    @Column(name = "original_text", nullable = false)
    private String originalText;

    @Lob
    @Column(name = "translated_text")
    private String translatedText;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Message(Long projectId, Long senderId, Long receiverId, String originalText, String translatedText) {
        this.projectId = projectId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.read = false;
    }

    public static Message create(Long projectId, Long senderId, Long receiverId, String originalText, String translatedText) {
        return new Message(projectId, senderId, receiverId, originalText, translatedText);
    }

    public void markAsRead() {
        this.read = true;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
