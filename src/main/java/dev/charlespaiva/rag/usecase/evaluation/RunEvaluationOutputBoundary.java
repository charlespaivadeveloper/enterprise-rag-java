package dev.charlespaiva.rag.usecase.evaluation;

@FunctionalInterface
public interface RunEvaluationOutputBoundary {
    void present(EvaluationReport report);
}
