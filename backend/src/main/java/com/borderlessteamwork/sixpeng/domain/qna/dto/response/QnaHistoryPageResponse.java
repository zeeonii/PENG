package com.borderlessteamwork.sixpeng.domain.qna.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QnaHistoryPageResponse {

    private List<QnaResponse> content;
    private boolean hasNext;
}
