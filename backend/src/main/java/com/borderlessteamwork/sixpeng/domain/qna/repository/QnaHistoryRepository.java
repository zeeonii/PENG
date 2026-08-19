package com.borderlessteamwork.sixpeng.domain.qna.repository;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaHistoryRepository extends JpaRepository<QnaHistory, Long> {

    Page<QnaHistory> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}
