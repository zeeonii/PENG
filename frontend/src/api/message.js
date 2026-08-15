/**
 * 쪽지 및 번역 관련 API입니다.
 *
 * 사용 예시:
 * import { getMessages, sendMessage, translate } from "../api/message.js";
 *
 * const { data } = await getMessages();
 * await sendMessage({ projectId, receiverId, originalText });
 * await markMessageAsRead(messageId);
 */

import client from "./client.js";

/** 쪽지 목록 조회 (받은/보낸) */
export const getMessages = (params) => client.get("/messages", { params });

/**
 * 쪽지 전송
 * @param {{ projectId: number, receiverId: number, originalText: string }} data
 */
export const sendMessage = (data) => client.post("/messages", data);

/** 쪽지 읽음 처리 */
export const markMessageAsRead = (messageId) =>
  client.patch(`/messages/${messageId}/read`);

/**
 * 메시지 변환 요청 (한국어 - 영어)
 * @param {{ text: string, senderLanguage?: string, receiverLanguage?: string }} data
 */
export const translate = (data) => client.post("/translation", data);
