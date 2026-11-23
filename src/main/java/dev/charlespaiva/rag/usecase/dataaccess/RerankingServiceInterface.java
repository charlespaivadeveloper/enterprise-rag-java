package dev.charlespaiva.rag.usecase.dataaccess;

import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import java.util.List;

@FunctionalInterface
public interface RerankingServiceInterface {
    List<RetrievedChunk> rerank(String query, List<RetrievedChunk> candidates);
}
