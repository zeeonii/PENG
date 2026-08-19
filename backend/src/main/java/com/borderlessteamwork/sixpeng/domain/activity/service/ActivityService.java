package com.borderlessteamwork.sixpeng.domain.activity.service;

import com.borderlessteamwork.sixpeng.domain.activity.dto.response.ActivityResponse;

import java.util.List;

public interface ActivityService {

    List<ActivityResponse> getRecentActivities(Long projectId, Long memberId);
}
