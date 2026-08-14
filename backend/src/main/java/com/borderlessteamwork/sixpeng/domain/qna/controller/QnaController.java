package com.borderlessteamwork.sixpeng.domain.qna.controller;

import com.borderlessteamwork.sixpeng.domain.qna.dto.request.QnaRequest;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaHistoryPageResponse;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaResponse;
import com.borderlessteamwork.sixpeng.domain.qna.service.QnaService;
import com.borderlessteamwork.sixpeng.global.response.ApiResponse;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @PostMapping
    public ApiResponse<QnaResponse> ask(
            @CurrentMember Long memberId,
            @PathVariable Long projectId,
            @Valid @RequestBody QnaRequest request
    ) {
        return ApiResponse.success(qnaService.ask(projectId, memberId, request.question()));
    }

    @GetMapping("/history")
    public ApiResponse<QnaHistoryPageResponse> getHistory(
            @CurrentMember Long memberId,
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(qnaService.getHistory(projectId, page, size));
    }
}
