package com.borderlessteamwork.sixpeng.domain.project.entity;

/**
 * 프로젝트 진행 상태.
 *
 * <p>표시 문구(진행전/진행중/완료)는 프론트가 매핑한다. 이 서비스는 번역이 핵심 기능이라
 * 한글 문구를 응답에 박아두면 다국어 대응이 막히므로, 다른 도메인
 * ({@code IntegrationConnectionStatus} 등)과 같이 enum 이름만 내려준다.
 */
public enum ProjectStatus {

    /** 진행전. 새로 만든 프로젝트의 기본값. */
    PENDING,

    /** 진행중 */
    IN_PROGRESS,

    /** 완료 */
    COMPLETED
}
