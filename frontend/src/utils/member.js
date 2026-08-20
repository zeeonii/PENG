/**
 * ProjectMember 표시 이름을 반환합니다.
 * 백엔드는 AI 팀원의 name을 "AI 팀원"으로 내려주므로, isAiTeammate일 때 "REMI"로 바꿔 보여줍니다.
 */
export const memberDisplayName = (member) => (member?.isAiTeammate ? "REMI" : member?.name);
