package com.borderlessteamwork.sixpeng.domain.project.repository;

import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
