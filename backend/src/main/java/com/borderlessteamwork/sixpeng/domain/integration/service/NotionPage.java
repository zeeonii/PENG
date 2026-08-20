package com.borderlessteamwork.sixpeng.domain.integration.service;

/** propertiesText: 데이터베이스(표)의 각 행(page)이 가진 속성값들을 사람이 읽을 수 있는 텍스트로 합친 것. */
record NotionPage(String id, String title, String url, String propertiesText) {
}
