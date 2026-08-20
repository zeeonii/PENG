/**
 * 백엔드 ProjectStatus enum(PENDING/IN_PROGRESS/COMPLETED)을 화면 표시용 한글로 바꿉니다.
 * 백엔드는 다국어 대응을 위해 enum 이름 그대로 내려주므로(한글 문구를 응답에 박지 않음),
 * 프론트에서 매핑해야 합니다.
 */
const STATUS_LABELS = {
  PENDING: "진행전",
  IN_PROGRESS: "진행중",
  COMPLETED: "완료",
};

export const projectStatusLabel = (status) => STATUS_LABELS[status] ?? status;
