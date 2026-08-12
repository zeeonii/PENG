package com.borderlessteamwork.sixpeng.domain.qna.controller;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.qna.dto.request.DocumentEmbedRequest;
import com.borderlessteamwork.sixpeng.domain.qna.dto.request.QnaRequest;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaResponse;
import com.borderlessteamwork.sixpeng.domain.qna.service.DocumentEmbeddingService;
import com.borderlessteamwork.sixpeng.domain.qna.service.QnaService;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
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
    private final DocumentEmbeddingService documentEmbeddingService;
    private final DocumentRepository documentRepository;

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

    /**
     * TODO: 임시 수동 검증용 엔드포인트. Document 저장 시점 자동 트리거 연동(이슈 #TBD) 완료되면 제거.
     */
    @PostMapping("/_debug/embed")
    public ApiResponse<Void> debugEmbed(
            @PathVariable Long projectId,
            @Valid @RequestBody DocumentEmbedRequest request
    ) {
        Document document = documentRepository.findById(request.documentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Document not found: id=" + request.documentId()));
        documentEmbeddingService.embed(document);
        return ApiResponse.success();
    }
}