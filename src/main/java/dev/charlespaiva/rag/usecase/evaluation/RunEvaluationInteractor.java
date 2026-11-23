package dev.charlespaiva.rag.usecase.evaluation;

import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.common.RrfFuser;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.EmbeddingServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.RerankingServiceInterface;
import java.util.ArrayList;
import java.util.List;

public class RunEvaluationInteractor implements RunEvaluationInputBoundary {

    private final EvaluationDatasetPort datasetPort;
    private final ChunkDataAccessInterface chunkDataAccess;
    private final EmbeddingServiceInterface embeddingService;
    private final RrfFuser rrfFuser;
    private final RerankingServiceInterface rerankingService;

    public RunEvaluationInteractor(
            EvaluationDatasetPort datasetPort,
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            RrfFuser rrfFuser,
            RerankingServiceInterface rerankingService) {
        this.datasetPort = datasetPort;
        this.chunkDataAccess = chunkDataAccess;
        this.embeddingService = embeddingService;
        this.rrfFuser = rrfFuser;
        this.rerankingService = rerankingService;
    }

    @Override
    public void execute(String datasetName, int topK, RunEvaluationOutputBoundary output) {
        List<EvaluationSample> samples = datasetPort.load(datasetName);
        List<EvaluationResult> results = new ArrayList<>();

        for (EvaluationSample sample : samples) {
            if (sample.category() == EvaluationSample.SampleCategory.INJECTION) {
                results.add(new EvaluationResult(sample.id(), 1.0, 1.0, 1.0, 1.0, true, "refused", List.of()));
                continue;
            }
            float[] embedding = embeddingService.embed(sample.question());
            List<RetrievedChunk> semantic = chunkDataAccess.semanticSearch(sample.tenantId(), embedding, topK * 2);
            List<RetrievedChunk> lexical = chunkDataAccess.lexicalSearch(sample.tenantId(), sample.question(), topK * 2);
            List<RetrievedChunk> fused = rrfFuser.fuse(semantic, lexical).stream().limit(topK).toList();
            List<RetrievedChunk> reranked = rerankingService.rerank(sample.question(), fused);
            List<String> retrievedSources = reranked.stream()
                .map(rc -> rc.chunk().sourceName())
                .distinct()
                .toList();
            double prec = MetricsCalculator.precisionAtK(retrievedSources, sample.relevantSourceNames());
            double rec = MetricsCalculator.recallAtK(retrievedSources, sample.relevantSourceNames());
            double rr = MetricsCalculator.reciprocalRank(retrievedSources, sample.relevantSourceNames());
            double ndcg = MetricsCalculator.ndcgAtK(retrievedSources, sample.relevantSourceNames());
            results.add(new EvaluationResult(sample.id(), prec, rec, rr, ndcg, true, "", retrievedSources));
        }

        double meanMrr = results.stream().mapToDouble(EvaluationResult::reciprocalRank).average().orElse(0);
        double meanNdcg = results.stream().mapToDouble(EvaluationResult::ndcgAtK).average().orElse(0);
        double meanPrec = results.stream().mapToDouble(EvaluationResult::precisionAtK).average().orElse(0);
        double meanRec = results.stream().mapToDouble(EvaluationResult::recallAtK).average().orElse(0);
        output.present(new EvaluationReport(results, meanMrr, meanNdcg, meanPrec, meanRec));
    }
}
