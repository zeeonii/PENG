/**
 * 외부 서비스 연동 관련 API입니다.
 *
 * 사용 예시:
 * import { getIntegrationStatus, connectNotion } from "../api/integration.js";
 *
 * const { data } = await getIntegrationStatus(projectId);
 *
 * // 연동 시작 및 브라우저 이동까지 한 번에 처리합니다.
 * await connectNotion(projectId);
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

/**
 * 브라우저를 authorizeUrl로 이동시킵니다. 컴포넌트 안에서 window를 직접
 * 건드리지 않도록 여기에 모아뒀습니다.
 */
export const goToAuthorizeUrl = (authorizeUrl) => {
  window.location.href = authorizeUrl;
};

/** Notion 연동 시작 + 이동까지 한 번에 */
export const connectNotion = async (projectId) => {
  const { data } = await startNotionIntegration(projectId);
  goToAuthorizeUrl(data.authorizeUrl);
};

/** Google Meet 연동 시작 + 이동까지 한 번에 */
export const connectGoogleMeet = async (projectId) => {
  const { data } = await startGoogleMeetIntegration(projectId);
  goToAuthorizeUrl(data.authorizeUrl);
};
