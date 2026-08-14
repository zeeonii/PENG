package com.borderlessteamwork.sixpeng.domain.integration.service;

import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Meet REST API(v2)에서 회의 기록(conferenceRecords)과 녹취록(transcripts)을 조회한다.
 * 이 스코프(meetings.space.readonly)로는 실제로 녹화/받아쓰기가 켜져 있던 회의만 조회된다
 * (Google Workspace Business Standard 이상 + 녹취록 사용 설정 필요).
 */
@Component
class GoogleMeetContentClient {

    private final WebClient webClient;
    private final GoogleDocsClient googleDocsClient;

    GoogleMeetContentClient(WebClient.Builder webClientBuilder, GoogleDocsClient googleDocsClient) {
        this.webClient = webClientBuilder.baseUrl("https://meet.googleapis.com/v2").build();
        this.googleDocsClient = googleDocsClient;
    }

    /** 최근 회의 기록 목록. 페이지네이션은 다루지 않고 첫 페이지(기본 page size)만 가져온다. */
    List<GoogleConferenceRecord> fetchRecentConferenceRecords(String accessToken) {
        JsonNode response = get(accessToken, "/conferenceRecords");

        List<GoogleConferenceRecord> records = new ArrayList<>();
        JsonNode conferenceRecords = response.path("conferenceRecords");
        if (conferenceRecords.isArray()) {
            for (JsonNode record : conferenceRecords) {
                records.add(new GoogleConferenceRecord(
                        record.path("name").asString(""),
                        record.path("startTime").asString("")
                ));
            }
        }
        return records;
    }

    /**
     * 해당 회의 기록의 첫 번째 녹취록 본문을 발화자: 내용 형태로 이어붙인다. 녹취록이 없으면 빈 문자열.
     * entries API는 구조화된 발화 기록이 있을 때만 채워지고, 실제로는 Google Docs 파일로만
     * export된 경우(docsDestination)가 있어 그 경우 Docs API로 본문을 대신 읽어온다.
     */
    String fetchTranscriptContent(String accessToken, String conferenceRecordName) {
        JsonNode transcriptsResponse = get(accessToken, "/" + conferenceRecordName + "/transcripts");
        JsonNode transcripts = transcriptsResponse.path("transcripts");
        if (!transcripts.isArray() || transcripts.isEmpty()) {
            return "";
        }
        JsonNode transcript = transcripts.get(0);
        String transcriptName = transcript.path("name").asString("");
        if (transcriptName.isEmpty()) {
            return "";
        }

        String entriesContent = fetchEntriesContent(accessToken, transcriptName);
        if (!entriesContent.isBlank()) {
            return entriesContent;
        }

        String docId = transcript.path("docsDestination").path("document").asString("");
        if (docId.isEmpty()) {
            return "";
        }
        return googleDocsClient.fetchPlainText(accessToken, docId);
    }

    private String fetchEntriesContent(String accessToken, String transcriptName) {
        JsonNode entriesResponse = get(accessToken, "/" + transcriptName + "/entries");
        StringBuilder content = new StringBuilder();
        JsonNode entries = entriesResponse.path("entries");
        if (entries.isArray()) {
            for (JsonNode entry : entries) {
                String participant = entry.path("participant").asString("");
                String text = entry.path("text").asString("");
                if (!text.isBlank()) {
                    content.append(participant).append(": ").append(text).append(System.lineSeparator());
                }
            }
        }
        return content.toString();
    }

    private JsonNode get(String accessToken, String uri) {
        try {
            JsonNode response = webClient.get()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (response == null) {
                throw new BusinessException(ErrorCode.GOOGLE_MEET_SYNC_FAILED);
            }
            return response;
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.GOOGLE_MEET_SYNC_FAILED);
        }
    }
}
