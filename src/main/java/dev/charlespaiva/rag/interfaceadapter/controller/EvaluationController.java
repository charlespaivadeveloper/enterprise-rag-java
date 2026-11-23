package dev.charlespaiva.rag.interfaceadapter.controller;

import dev.charlespaiva.rag.interfaceadapter.presenter.EvaluationPresenter;
import dev.charlespaiva.rag.usecase.evaluation.EvaluationReport;
import dev.charlespaiva.rag.usecase.evaluation.RunEvaluationInputBoundary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eval")
@ConditionalOnProperty(name = "rag.eval.enabled", havingValue = "true")
@Tag(name = "Evaluation", description = "RAG quality evaluation — enabled by rag.eval.enabled=true")
public class EvaluationController {

    private final RunEvaluationInputBoundary evaluationBoundary;

    public EvaluationController(RunEvaluationInputBoundary evaluationBoundary) {
        this.evaluationBoundary = evaluationBoundary;
    }

    @Operation(summary = "Run evaluation against a named golden dataset")
    @PostMapping("/run")
    public EvaluationReport run(
        @RequestParam(defaultValue = "golden-v1") String dataset,
        @RequestParam(defaultValue = "5") @Min(1) @Max(20) int topK) {
        EvaluationPresenter presenter = new EvaluationPresenter();
        evaluationBoundary.execute(dataset, topK, presenter);
        return presenter.getReport();
    }
}
