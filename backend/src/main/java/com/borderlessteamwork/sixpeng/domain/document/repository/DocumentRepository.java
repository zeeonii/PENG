package com.borderlessteamwork.sixpeng.domain.document.repository;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
