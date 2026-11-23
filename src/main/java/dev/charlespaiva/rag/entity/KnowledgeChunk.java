package dev.charlespaiva.rag.entity;

import java.util.UUID;

public record KnowledgeChunk(UUID id, String tenantId, UUID documentId, int ordinal, String content, String sourceName) {
    public KnowledgeChunk {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
    }
}
