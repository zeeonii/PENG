/**
 * 설정 화면 mock 데이터입니다.
 * 필드명은 docs/openapi.yaml의 Member 스키마를 따릅니다.
 * 실제 연동 시 GET /accounts/me 응답으로 교체하면 됩니다.
 */

// GET /accounts/me
export const currentMember = {
  id: 1,
  email: "hyesun@example.com",
  name: "안혜선",
  language: "KR",
  country: "대한민국",
  timezone: "(GMT+9) 서울",
  duty: "백엔드 개발",
};

export const COUNTRY_OPTIONS = [
  "대한민국",
  "미국",
  "일본",
  "싱가포르",
];

export const TIMEZONE_OPTIONS = [
  "(GMT+9) 서울",
  "(GMT+9) 도쿄",
  "(GMT+8) 싱가포르",
  "(GMT-8) 로스앤젤레스",
];

export const LANGUAGE_OPTIONS = [
  { value: "KR", label: "한국어" },
  { value: "EN", label: "English" },
];

/**
 * Cultural Translation 표시 설정입니다.
 * 명세의 MemberUpdateRequest에는 없는 항목이라 저장 위치 확인이 필요합니다. (백엔드 확인 필요)
 */
export const translationDisplay = {
  showOriginal: true,
  showTranslated: true,
};

// 설정 > 연동 관리에서 사용 (GET /integrations/status)
export const settingIntegrations = [
  {
    type: "GOOGLE_MEET",
    description: "회의록 자동 연동",
    status: "DISCONNECTED",
    notice:
      "Google Workspace Business Standard 이상 플랜에서만 회의록 연동이 가능합니다. 현재 연결한 계정은 개인 Gmail 계정으로 확인되어 연동이 제한됩니다.",
    lastSyncedAt: null,
  },
  {
    type: "NOTION",
    description: "문서 자동 연동",
    status: "CONNECTED",
    notice: null,
    lastSyncedAt: "10분 전",
  },
];
