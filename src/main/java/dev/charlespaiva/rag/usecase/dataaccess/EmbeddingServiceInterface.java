package dev.charlespaiva.rag.usecase.dataaccess;

import java.util.List;

public interface EmbeddingServiceInterface {
    List<float[]> embed(List<String> texts);
    float[] embed(String text);
}
