package dev.charlespaiva.rag.usecase.common;

import dev.charlespaiva.rag.entity.KnowledgeChunk;

public record RetrievedChunk(KnowledgeChunk chunk, double semanticScore, double lexicalScore, double finalScore) {
}
