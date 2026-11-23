package dev.charlespaiva.rag.usecase.dataaccess;

import dev.charlespaiva.rag.entity.KnowledgeChunk;
import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import java.util.List;
import java.util.UUID;

public interface ChunkDataAccessInterface {
    void saveAll(List<KnowledgeChunk> chunks, List<float[]> embeddings);
    List<RetrievedChunk> hybridSearch(String tenantId, String query, float[] embedding, int limit);
    List<RetrievedChunk> semanticSearch(String tenantId, float[] embedding, int limit);
    List<RetrievedChunk> lexicalSearch(String tenantId, String query, int limit);
    PagedResult<DocumentSummaryData> listDocuments(String tenantId, int page, int size);
    int deleteByDocument(String tenantId, UUID documentId);
}
