/**
 * 계정 관련 API입니다.
 *
 * 사용 예시:
 * import { getMe, goToGoogleLogin } from "../api/member.js";
 *
 * goToGoogleLogin();                    // 로그인 버튼 클릭 시
 * const { data } = await getMe();       // 로그인 사용자 정보
 * await updateMe({ language: "EN" });   // 프로필 수정
 */

import client, { API_BASE_URL } from "./client.js";

/**
 * Google 로그인 시작.
 * OAuth는 브라우저가 실제로 이동해야 하므로 axios가 아닌 페이지 이동을 사용합니다.
 * (fetch/axios로 호출하면 리다이렉트가 동작하지 않습니다)
 */
export const goToGoogleLogin = () => {
  window.location.href = `${API_BASE_URL}/accounts/oauth/google`;
};

/** 로그아웃 */
export const logout = () => client.post("/accounts/logout");

/** 내 정보 조회 */
export const getMe = () => client.get("/accounts/me");

/**
 * 프로필 수정 (언어/국가/타임존/담당업무)
 * @param {{ language?: string, country?: string, timezone?: string, duty?: string }} data
 */
export const updateMe = (data) => client.patch("/accounts/me", data);
