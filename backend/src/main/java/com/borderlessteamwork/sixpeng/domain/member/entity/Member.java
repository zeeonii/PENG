package com.borderlessteamwork.sixpeng.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    // MySQL 네이티브 enum 으로 만들면 값을 추가할 때 ddl-auto 가 컬럼을 바꿔주지 못하므로 varchar 로 둔다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Language language;

    private String country;

    private String timezone;

    /** 담당 업무 */
    private String duty;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Member(String googleId, String email, String name, Language language) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.language = language;
    }

    public static Member ofGoogle(String googleId, String email, String name, Language language) {
        return new Member(googleId, email, name, language);
    }

    /** 재로그인 시 Google 쪽에서 바뀐 값을 반영한다. */
    public void syncGoogleProfile(String email, String name) {
        this.email = email;
        this.name = name;
    }

    /** PATCH 시맨틱: null 인 필드는 변경하지 않는다. */
    public void updateProfile(Language language, String country, String timezone, String duty) {
        if (language != null) {
            this.language = language;
        }
        if (country != null) {
            this.country = country;
        }
        if (timezone != null) {
            this.timezone = timezone;
        }
        if (duty != null) {
            this.duty = duty;
        }
    }
}
