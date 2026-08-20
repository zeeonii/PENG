package com.borderlessteamwork.sixpeng.domain.integration.service;

/** sourceUrl: 녹취록이 Google Docs로 export된 경우(docsDestination)에만 채워지고, 그 외에는 null. */
record GoogleMeetTranscript(String content, String sourceUrl) {

    static GoogleMeetTranscript empty() {
        return new GoogleMeetTranscript("", null);
    }
}
