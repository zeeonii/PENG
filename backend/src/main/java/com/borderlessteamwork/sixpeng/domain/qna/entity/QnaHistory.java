package com.borderlessteamwork.sixpeng.domain.qna.entity;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "qna_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * ddl-auto: update는 이미 만들어진 컬럼의 타입을 넓혀주지 못하므로, @Lob만으로는
     * 부족할 수 있어 컬럼 타입을 명시한다 (document.content에서 겪은 것과 동일한 문제).
     */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String answer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public QnaHistory(Project project, Member member, String question, String answer) {
        this.project = project;
        this.member = member;
        this.question = question;
        this.answer = answer;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
