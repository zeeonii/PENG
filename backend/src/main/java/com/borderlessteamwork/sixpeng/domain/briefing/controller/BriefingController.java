package com.borderlessteamwork.sixpeng.domain.briefing.controller;

import com.borderlessteamwork.sixpeng.domain.briefing.dto.response.BriefingDetailResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.dto.response.BriefingSummaryResponse;
import com.borderlessteamwork.sixpeng.domain.briefing.service.BriefingService;
import com.borderlessteamwork.sixpeng.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/briefings")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService briefingService;

    @GetMapping("/today")
    public ApiResponse<List<BriefingSummaryResponse>> getTodayBriefings(
            @PathVariable Long projectId,
            @RequestParam Long memberId
    ) {
        return ApiResponse.success(briefingService.getTodayBriefings(projectId, memberId));
    }

    @GetMapping("/{briefingId}")
    public ApiResponse<BriefingDetailResponse> getBriefingDetail(
            @PathVariable Long projectId,
            @PathVariable Long briefingId
    ) {
        return ApiResponse.success(briefingService.getBriefingDetail(briefingId));
    }
}
