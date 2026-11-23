package dev.charlespaiva.rag.usecase.evaluation;

import java.util.List;

@FunctionalInterface
public interface EvaluationDatasetPort {
    List<EvaluationSample> load(String datasetName);
}
