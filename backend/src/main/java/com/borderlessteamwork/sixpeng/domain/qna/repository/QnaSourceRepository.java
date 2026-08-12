package com.borderlessteamwork.sixpeng.domain.qna.repository;

import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaSourceRepository extends JpaRepository<QnaSource, Long> {

    List<QnaSource> findByQnaHistoryId(Long qnaHistoryId);
}
