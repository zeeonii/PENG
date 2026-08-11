package com.borderlessteamwork.sixpeng.domain.message.controller;

import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(@RequestHeader("X-Member-Id") Long memberId) {
        return ResponseEntity.ok(messageService.getMessages(memberId));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody MessageCreateRequest request
    ) {
        MessageResponse response = messageService.sendMessage(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }
}
