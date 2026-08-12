package com.borderlessteamwork.sixpeng.domain.qna.service;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    private TextChunker() {
    }

    public static List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(text.substring(start, end));

            if (end == length) {
                break;
            }
            start = end - OVERLAP;
        }

        return chunks;
    }
}
