package com.borderlessteamwork.sixpeng.domain.qna.repository;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaHistoryRepository extends JpaRepository<QnaHistory, Long> {

    List<QnaHistory> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<QnaHistory> findByProjectIdAndMemberIdOrderByCreatedAtDesc(Long projectId, Long memberId);
}
