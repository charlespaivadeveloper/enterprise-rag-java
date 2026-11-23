package dev.charlespaiva.rag.usecase.common;

import java.util.ArrayList;
import java.util.List;

public class DocumentChunker {

    public List<String> split(String text, int size, int overlap) {
        if (size < 100 || overlap < 0 || overlap >= size) {
            throw new IllegalArgumentException("invalid chunk configuration");
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        int pos = 0;
        while (pos < normalized.length()) {
            int end = Math.min(pos + size, normalized.length());
            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf(' ', end);
                if (boundary > pos + size / 2) {
                    end = boundary;
                }
            }
            chunks.add(normalized.substring(pos, end).trim());
            if (end == normalized.length()) {
                break;
            }
            pos = Math.max(end - overlap, pos + 1);
        }
        return chunks;
    }
}
