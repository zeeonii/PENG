package com.borderlessteamwork.sixpeng.domain.briefing.entity;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "briefing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Briefing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String summary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Briefing(Project project, Member member, String summary) {
        this.project = project;
        this.member = member;
        this.summary = summary;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
