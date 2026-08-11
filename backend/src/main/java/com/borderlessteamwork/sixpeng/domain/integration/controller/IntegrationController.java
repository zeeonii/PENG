package com.borderlessteamwork.sixpeng.domain.integration.controller;

import com.borderlessteamwork.sixpeng.domain.integration.dto.response.IntegrationStatusResponse;
import com.borderlessteamwork.sixpeng.domain.integration.dto.response.AuthorizeUrlResponse;
import com.borderlessteamwork.sixpeng.domain.integration.service.IntegrationService;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @PostMapping("/projects/{projectId}/integrations/notion")
    public ResponseEntity<AuthorizeUrlResponse> startNotionConnection(
            @CurrentMember Long memberId, @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(integrationService.startNotionConnection(projectId, memberId));
    }

    @GetMapping("/integrations/notion/callback")
    public ResponseEntity<Void> handleNotionCallback(
            @CurrentMember Long memberId,
            @RequestParam String code,
            @RequestParam String state
    ) {
        String redirectUrl = integrationService.handleNotionCallback(code, state, memberId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/projects/{projectId}/integrations/google-meet")
    public ResponseEntity<AuthorizeUrlResponse> startGoogleMeetConnection(
            @CurrentMember Long memberId, @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(integrationService.startGoogleMeetConnection(projectId, memberId));
    }

    @GetMapping("/integrations/google-meet/callback")
    public ResponseEntity<Void> handleGoogleMeetCallback(
            @CurrentMember Long memberId,
            @RequestParam String code,
            @RequestParam String state
    ) {
        String redirectUrl = integrationService.handleGoogleMeetCallback(code, state, memberId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/projects/{projectId}/integrations/status")
    public ResponseEntity<List<IntegrationStatusResponse>> getStatus(
            @CurrentMember Long memberId, @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(integrationService.getStatus(projectId, memberId));
    }
}
