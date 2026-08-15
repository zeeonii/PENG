/**
 * 컨텍스트 Q&A 관련 API입니다.
 *
 * 사용 예시:
 * import { askQuestion, getQnaHistory } from "../api/qna.js";
 *
 * const { data } = await askQuestion(projectId, { question: "왜 JWT를 쓰나요?" });
 * const { data } = await getQnaHistory(projectId);
 */

import client from "./client.js";

/**
 * 질문 전송 및 답변 조회
 * @param {{ question: string }} data
 */
export const askQuestion = (projectId, data) =>
  client.post(`/projects/${projectId}/qna`, data);

/** 대화 히스토리 조회 */
export const getQnaHistory = (projectId, params) =>
  client.get(`/projects/${projectId}/qna/history`, { params });
