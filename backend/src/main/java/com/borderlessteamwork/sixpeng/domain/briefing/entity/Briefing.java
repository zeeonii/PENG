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

    /**
     * 새 문서가 있었지만 담당 업무와 무관하다고 LLM이 판단했을 때도(RELEVANT_NONE)
     * true/false와 무관하게 항상 row를 하나 저장해 "마지막으로 검토한 시점" 커서를
     * 앞으로 옮긴다. 그래야 같은 문서 묶음을 다음 조회 때 또 OpenAI에 보내지 않는다.
     * hasUpdate=false인 row는 실제 브리핑 내용이 아니므로 화면(오늘의 브리핑,
     * 최근 활동)에는 노출하지 않는다.
     */
    @Column(nullable = false)
    private boolean hasUpdate = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Briefing(Project project, Member member, String summary) {
        this.project = project;
        this.member = member;
        this.summary = summary;
    }

    /** RELEVANT_NONE 판단 결과를 저장하는 "검토 완료" 표시용 row에서만 호출한다. */
    public void markNoUpdate() {
        this.hasUpdate = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
