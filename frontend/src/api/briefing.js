/**
 * AI 브리핑 관련 API입니다.
 *
 * 사용 예시:
 * import { getTodayBriefing } from "../api/briefing.js";
 *
 * const { data } = await getTodayBriefing(projectId);
 */

import client from "./client.js";

/** 오늘의 브리핑 조회 */
export const getTodayBriefing = (projectId) =>
  client.get(`/projects/${projectId}/briefings/today`);

/** 브리핑 상세 조회 */
export const getBriefing = (projectId, briefingId) =>
  client.get(`/projects/${projectId}/briefings/${briefingId}`);
