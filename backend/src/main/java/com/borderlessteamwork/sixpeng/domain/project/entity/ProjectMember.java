package com.borderlessteamwork.sixpeng.domain.project.entity;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_member_project_id_member_id",
                columnNames = {"project_id", "member_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** PM, Backend, Design 등 */
    private String role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    private ProjectMember(Project project, Member member, String role) {
        this.project = project;
        this.member = member;
        this.role = role;
    }

    public static ProjectMember of(Project project, Member member, String role) {
        return new ProjectMember(project, member, role);
    }

    public void updateRole(String role) {
        this.role = role;
    }
}
