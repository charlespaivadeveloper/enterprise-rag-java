package dev.charlespaiva.rag.usecase.evaluation;

import java.util.List;

public record EvaluationReport(
    List<EvaluationResult> results,
    double meanReciprocalRank,
    double meanNdcg,
    double meanPrecision,
    double meanRecall
) {
    public EvaluationReport {
        results = List.copyOf(results);
    }
}
