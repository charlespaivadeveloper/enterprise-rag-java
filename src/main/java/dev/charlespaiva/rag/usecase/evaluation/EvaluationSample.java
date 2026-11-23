package dev.charlespaiva.rag.usecase.evaluation;

import java.util.List;

public record EvaluationSample(
    String id,
    String tenantId,
    String question,
    List<String> relevantSourceNames,
    String expectedAnswerContains,
    SampleCategory category
) {
    public EvaluationSample {
        relevantSourceNames = List.copyOf(relevantSourceNames);
    }

    public enum SampleCategory {
        NORMAL, UNANSWERABLE, CROSS_TENANT, INJECTION
    }
}
