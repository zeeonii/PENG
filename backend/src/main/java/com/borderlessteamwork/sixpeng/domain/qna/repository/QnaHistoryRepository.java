package com.borderlessteamwork.sixpeng.domain.qna.repository;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaHistoryRepository extends JpaRepository<QnaHistory, Long> {

    Page<QnaHistory> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    /** 프로젝트 삭제 시 QnA 기록을 정리하는 용도. qna_source가 FK로 참조하므로 먼저 지워야 한다. */
    void deleteByProjectId(Long projectId);
}
