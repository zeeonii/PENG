package com.borderlessteamwork.sixpeng.domain.message.repository;

import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);

    /** 프로젝트 삭제 시 메시지 행을 정리하는 용도. */
    void deleteByProjectId(Long projectId);
}
