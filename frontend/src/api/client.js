/**
 * API 요청 공통 설정입니다.
 *
 * 사용 예시:
 * import client from "./client.js";
 * client.get("/projects");
 *
 * withCredentials: 세션 쿠키를 함께 전송합니다. 백엔드가 SameSite=None; Secure로
 * 쿠키를 내려주므로 모든 요청에 필요합니다.
 *
 * CSRF: axios가 XSRF-TOKEN 쿠키를 읽어 X-XSRF-TOKEN 헤더로 자동 전송합니다.
 * (POST/PATCH/DELETE에 필요하며, 누락 시 403이 발생합니다)
 *
 * 로그인은 이 client로 호출하지 않습니다. OAuth 리다이렉트가 필요하므로
 * api/member.js의 goToGoogleLogin()을 사용해주세요.
 */

import axios from "axios";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const client = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
});

export default client;
