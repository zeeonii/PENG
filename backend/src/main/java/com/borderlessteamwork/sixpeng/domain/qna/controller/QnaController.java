package com.borderlessteamwork.sixpeng.domain.qna.controller;

import com.borderlessteamwork.sixpeng.domain.qna.dto.request.QnaRequest;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaResponse;
import com.borderlessteamwork.sixpeng.domain.qna.service.QnaService;
import com.borderlessteamwork.sixpeng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @PostMapping
    public ApiResponse<QnaResponse> ask(
            @PathVariable Long projectId,
            @Valid @RequestBody QnaRequest request
    ) {
        return ApiResponse.success(qnaService.ask(projectId, request.memberId(), request.question()));
    }

    @GetMapping("/history")
    public ApiResponse<List<QnaResponse>> getHistory(@PathVariable Long projectId) {
        return ApiResponse.success(qnaService.getHistory(projectId));
    }
}
