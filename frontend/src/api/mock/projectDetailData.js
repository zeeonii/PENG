/**
 * 프로젝트 상세 탭 mock 데이터입니다.
 * 필드명은 docs/openapi.yaml 스키마를 따르며, 명세에 없는 항목은 주석으로 표시했습니다.
 * 실제 API 연동 시 이 파일의 데이터를 API 호출 결과로 교체하면 됩니다.
 */

// GET /projects/{projectId}/briefings/today, /briefings/{briefingId}
// title, impact는 와이어프레임에 있으나 명세에 없는 필드입니다. (백엔드 확인 필요)
export const briefings = [
  {
    id: 1,
    title: "로그인 정책이 변경되었습니다",
    summary:
      "당신이 담당하는 인증 모듈에 직접 영향을 미칩니다. 세션 방식에서 JWT 방식으로 전환되어 관련 코드 수정이 필요합니다.",
    impact: "HIGH",
    sources: [
      {
        documentId: 101,
        title: "API 설계 논의 회의록 · 발언자: 김지훈",
        sourceUrl: "#",
        occurredAt: "2024-03-12",
      },
    ],
    createdAt: "2024-03-12T09:02:00+09:00",
  },
  {
    id: 2,
    title: "온보딩 가이드 문서가 수정되었습니다",
    summary:
      "신규 팀원 온보딩 절차 중 계정 연동 단계가 변경되어, 다음 온보딩부터 안내 스크립트 수정이 필요합니다.",
    impact: "MEDIUM",
    sources: [
      {
        documentId: 102,
        title: "온보딩 가이드 Notion 문서 · 편집자: Sarah Lee",
        sourceUrl: "#",
        occurredAt: "2024-03-12",
      },
    ],
    createdAt: "2024-03-12T08:50:00+09:00",
  },
];

// 영향도 표기 및 Badge variant 매핑
export const impactLabel = { HIGH: "영향도 높음", MEDIUM: "영향도 보통", LOW: "영향도 낮음" };
export const impactVariant = { HIGH: "danger", MEDIUM: "default", LOW: "success" };

// GET /projects/{projectId}/qna/history
export const qnaHistory = [
  {
    question: "왜 JWT를 쓰기로 했나요?",
    answer:
      "API 인증 방식은 세션 대신 JWT로 결정되었습니다. 확장성과 무상태(stateless) 인증이 필요했기 때문입니다.",
    sources: [
      { documentId: 101, title: "API 설계 논의 회의록", sourceUrl: "#", occurredAt: "2024-03-12" },
      { documentId: 103, title: "결정사항 문서", sourceUrl: "#", occurredAt: "2024-03-12" },
    ],
    createdAt: "2024-03-12T10:20:00+09:00",
  },
];

// GET /projects/{projectId}/members
// duty(담당 업무)는 와이어프레임에 있으나 명세에 없는 필드입니다. (백엔드 확인 필요)
export const projectMembers = [
  {
    memberId: 0,
    name: "AI 팀원",
    duty: "전체 맥락",
    country: "항상 온라인",
    timezone: "",
    role: "AUTO",
  },
  {
    memberId: 1,
    name: "김지훈",
    duty: "백엔드 개발",
    country: "한국",
    timezone: "KST",
    role: "ADMIN",
  },
  {
    memberId: 2,
    name: "Sarah Lee",
    duty: "디자인",
    country: "미국",
    timezone: "PST",
    role: "MEMBER",
  },
  {
    memberId: 3,
    name: "다나카 유키",
    duty: "UX 리서치",
    country: "일본",
    timezone: "JST",
    role: "MEMBER",
  },
  {
    memberId: 4,
    name: "라이언 첸",
    duty: "PM",
    country: "싱가포르",
    timezone: "SGT",
    role: "MEMBER",
  },
];

export const roleLabel = { AUTO: "자동 참여", ADMIN: "관리자", MEMBER: "멤버" };

// GET /integrations/status
export const integrations = [
  {
    type: "GOOGLE_MEET",
    status: "CONNECTED",
    lastSyncedAt: "방금 전",
  },
  {
    type: "NOTION",
    status: "CONNECTED",
    lastSyncedAt: "10분 전",
  },
];

export const integrationLabel = { GOOGLE_MEET: "Google Meet", NOTION: "Notion" };
export const integrationStatusLabel = { CONNECTED: "정상", DISCONNECTED: "연결 끊김" };
export const integrationStatusVariant = { CONNECTED: "success", DISCONNECTED: "danger" };

// 동기화 로그 조회 API는 명세에 없습니다. (백엔드 확인 필요)
export const syncLogs = [
  { id: 1, message: "Google Meet 회의록 1건 동기화 완료", occurredAt: "오늘 오전 9:00" },
  { id: 2, message: "Notion 문서 2건 동기화 완료", occurredAt: "오늘 오전 8:50" },
  { id: 3, message: "Google Meet 회의록 3건 동기화 완료", occurredAt: "어제 오후 10:15" },
];
