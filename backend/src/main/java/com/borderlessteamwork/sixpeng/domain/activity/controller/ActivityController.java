package com.borderlessteamwork.sixpeng.domain.activity.controller;

import com.borderlessteamwork.sixpeng.domain.activity.dto.response.ActivityResponse;
import com.borderlessteamwork.sixpeng.domain.activity.service.ActivityService;
import com.borderlessteamwork.sixpeng.global.security.CurrentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/projects/{projectId}/activities")
    public List<ActivityResponse> getRecentActivities(
            @CurrentMember Long memberId, @PathVariable Long projectId
    ) {
        return activityService.getRecentActivities(projectId, memberId);
    }
}
