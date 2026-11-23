package dev.charlespaiva.rag.interfaceadapter.presenter;

import dev.charlespaiva.rag.usecase.evaluation.EvaluationReport;
import dev.charlespaiva.rag.usecase.evaluation.RunEvaluationOutputBoundary;

public class EvaluationPresenter implements RunEvaluationOutputBoundary {

    private EvaluationReport report;

    @Override
    public void present(EvaluationReport evaluationReport) {
        this.report = evaluationReport;
    }

    public EvaluationReport getReport() {
        return report;
    }
}
