package com.borderlessteamwork.sixpeng.domain.qna.repository;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaSourceRepository extends JpaRepository<QnaSource, Long> {

    List<QnaSource> findByQnaHistoryId(Long qnaHistoryId);

    /** 프로젝트 삭제 시 QnA 기록을 지우기 전에 먼저 정리해야 하는 자식 행. */
    void deleteByQnaHistoryProjectId(Long projectId);
}
