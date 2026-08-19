/**
 * 프로젝트 홈 "최근 활동" 피드 API입니다.
 *
 * 사용 예시:
 * import { getRecentActivities } from "../api/activity.js";
 *
 * const { data } = await getRecentActivities(projectId);
 *
 * document(Notion/Google Meet 수집)와 briefing(브리핑 생성) 이벤트를
 * 발생 시각 기준 최신순으로 합쳐 최대 10건 반환합니다.
 */

import client from "./client.js";

export const getRecentActivities = (projectId) => client.get(`/projects/${projectId}/activities`);
