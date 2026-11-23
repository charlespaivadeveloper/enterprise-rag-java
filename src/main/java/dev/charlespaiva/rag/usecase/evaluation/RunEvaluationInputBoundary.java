package dev.charlespaiva.rag.usecase.evaluation;

@FunctionalInterface
public interface RunEvaluationInputBoundary {
    void execute(String datasetName, int topK, RunEvaluationOutputBoundary output);
}
