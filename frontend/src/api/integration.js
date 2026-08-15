/**
 * 외부 서비스 연동 관련 API입니다.
 *
 * 사용 예시:
 * import { getIntegrationStatus, startNotionIntegration } from "../api/integration.js";
 *
 * const { data } = await getIntegrationStatus(projectId);
 *
 * // 연동 시작: 응답의 authorizeUrl로 브라우저를 이동시켜 사용자 동의를 받습니다.
 * const { data } = await startNotionIntegration(projectId);
 * window.location.href = data.authorizeUrl;
 *
 * 콜백(/integrations/*\/callback)은 외부 서비스가 호출하므로 프론트에서 부르지 않습니다.
 */

import client from "./client.js";

/** 연동 상태 조회 (프로젝트 단위) */
export const getIntegrationStatus = (projectId) =>
  client.get(`/projects/${projectId}/integrations/status`);

/** Notion 연동 시작 — 응답의 authorizeUrl로 이동시켜야 합니다 */
export const startNotionIntegration = (projectId) =>
  client.post(`/projects/${projectId}/integrations/notion`);

/** Google Meet 연동 시작 — 응답의 authorizeUrl로 이동시켜야 합니다 */
export const startGoogleMeetIntegration = (projectId) =>
  client.post(`/projects/${projectId}/integrations/google-meet`);
