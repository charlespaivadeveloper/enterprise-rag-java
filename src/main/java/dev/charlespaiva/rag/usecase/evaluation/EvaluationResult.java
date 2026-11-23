package dev.charlespaiva.rag.usecase.evaluation;

import java.util.List;

public record EvaluationResult(
    String sampleId,
    double precisionAtK,
    double recallAtK,
    double reciprocalRank,
    double ndcgAtK,
    boolean groundednessPass,
    String actualAnswer,
    List<String> retrievedSourceNames
) {
    public EvaluationResult {
        retrievedSourceNames = List.copyOf(retrievedSourceNames);
    }
}
