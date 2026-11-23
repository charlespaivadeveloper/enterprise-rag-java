package dev.charlespaiva.rag.usecase.dataaccess;

import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import java.util.List;

@FunctionalInterface
public interface GenerationServiceInterface {
    String generate(String question, List<RetrievedChunk> evidence);
}
