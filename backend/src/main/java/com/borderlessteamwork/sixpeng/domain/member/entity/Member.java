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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    /**
     * AI 팀원 계정을 찾는 기준값. Google 로그인으로는 절대 나올 수 없는 예약어라
     * 실제 사용자 계정과 충돌하지 않는다. 다른 도메인(qna, briefing 등)에서
     * 'AI 가 한 일'을 표시할 때 이 값으로 회원을 조회하면 된다.
     */
    public static final String AI_TEAMMATE_GOOGLE_ID = "SYSTEM_AI_TEAMMATE";
    public static final String AI_TEAMMATE_EMAIL = "ai@sixpeng.internal";
    public static final String AI_TEAMMATE_NAME = "AI 팀원";

    /**
     * 요구사항 5-7: 미설정 시 시스템 기본값. Google 은 timezone 을 주지 않으므로
     * 가입 시점에는 항상 이 값이 들어가고, 사용자가 프로필에서 바꾸면 그 값이 유지된다.
     * 언어 기본값(EN)은 {@link Language#fromLocale(String)} 이 담당한다.
     */
    public static final String DEFAULT_TIMEZONE = "UTC";

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

    /** Google 이 국가 정보를 주지 않아 기본값 없이 비워 둔다. 사용자가 프로필에서 채운다. */
    private String country;

    @ColumnDefault("'" + DEFAULT_TIMEZONE + "'")
    private String timezone = DEFAULT_TIMEZONE;

    /** 담당 업무 */
    private String duty;

    /**
     * 번역 표시 토글. 둘 다 false 인 상태는 허용하지 않는다.
     * ColumnDefault 가 있어야 ddl-auto 가 컬럼을 추가할 때 기존 행이 false 로 채워지지 않는다.
     */
    @ColumnDefault("true")
    @Column(name = "show_original_text", nullable = false)
    private boolean showOriginalText = true;

    @ColumnDefault("true")
    @Column(name = "show_translated_text", nullable = false)
    private boolean showTranslatedText = true;

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

    /** 로그인 없이 시스템이 심어두는 AI 팀원 계정. */
    public static Member ofAiTeammate() {
        return new Member(AI_TEAMMATE_GOOGLE_ID, AI_TEAMMATE_EMAIL, AI_TEAMMATE_NAME, Language.EN);
    }

    public boolean isAiTeammate() {
        return AI_TEAMMATE_GOOGLE_ID.equals(googleId);
    }

    /** 재로그인 시 Google 쪽에서 바뀐 값을 반영한다. */
    public void syncGoogleProfile(String email, String name) {
        this.email = email;
        this.name = name;
    }

    /**
     * PATCH 시맨틱: null 인 필드는 변경하지 않는다.
     * 번역 표시 토글은 호출 전에 서비스에서 최종값을 계산·검증해 넘긴다.
     */
    public void updateProfile(Language language, String country, String timezone, String duty,
                              boolean showOriginalText, boolean showTranslatedText) {
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
        this.showOriginalText = showOriginalText;
        this.showTranslatedText = showTranslatedText;
    }
}
