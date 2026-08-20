package com.borderlessteamwork.sixpeng.domain.project.entity;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * MySQL 네이티브 enum 으로 만들면 값을 추가할 때 ddl-auto 가 컬럼을 바꿔주지 못하므로 varchar 로 둔다.
     *
     * <p>{@code @ColumnDefault} 가 없으면 ddl-auto: update 가 기존 행이 있는 테이블에
     * NOT NULL 컬럼을 추가할 때 빈 문자열이 채워지고, 그 값은 enum 으로 매핑되지 않아
     * 기존 프로젝트를 읽는 순간 터진다.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @ColumnDefault("'PENDING'")
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Project(String name, Member createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public static Project of(String name, Member createdBy) {
        return new Project(name, createdBy);
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public boolean isOwnedBy(Long memberId) {
        return createdBy.getId().equals(memberId);
    }
}
