package dev.charlespaiva.rag.framework.config;

import dev.charlespaiva.rag.usecase.ask.AskQuestionInputBoundary;
import dev.charlespaiva.rag.usecase.ask.AskQuestionInteractor;
import dev.charlespaiva.rag.usecase.common.DocumentChunker;
import dev.charlespaiva.rag.usecase.common.PromptInjectionGuard;
import dev.charlespaiva.rag.usecase.common.RrfFuser;
import dev.charlespaiva.rag.usecase.dataaccess.ChunkDataAccessInterface;
import dev.charlespaiva.rag.usecase.dataaccess.EmbeddingServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.GenerationServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.RerankingServiceInterface;
import dev.charlespaiva.rag.usecase.documents.DeleteDocumentInputBoundary;
import dev.charlespaiva.rag.usecase.documents.DeleteDocumentInteractor;
import dev.charlespaiva.rag.usecase.documents.ListDocumentsInputBoundary;
import dev.charlespaiva.rag.usecase.documents.ListDocumentsInteractor;
import dev.charlespaiva.rag.usecase.evaluation.EvaluationDatasetPort;
import dev.charlespaiva.rag.usecase.evaluation.RunEvaluationInputBoundary;
import dev.charlespaiva.rag.usecase.evaluation.RunEvaluationInteractor;
import dev.charlespaiva.rag.usecase.ingest.GetIngestJobStatusInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.GetIngestJobStatusInteractor;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentInteractor;
import dev.charlespaiva.rag.usecase.ingest.IngestJobDispatcherPort;
import dev.charlespaiva.rag.usecase.ingest.IngestJobRepositoryPort;
import dev.charlespaiva.rag.usecase.ingest.PiiRedactorPort;
import dev.charlespaiva.rag.usecase.ingest.SubmitIngestJobInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.SubmitIngestJobInteractor;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean
    public DocumentChunker documentChunker() {
        return new DocumentChunker();
    }

    @Bean
    public PromptInjectionGuard promptInjectionGuard() {
        return new PromptInjectionGuard();
    }

    @Bean
    public RrfFuser rrfFuser() {
        return new RrfFuser();
    }

    @Bean(name = "ingestExecutor")
    public Executor ingestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ingest-");
        executor.initialize();
        return executor;
    }

    @Bean
    public IngestDocumentInputBoundary ingestDocumentInputBoundary(
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            DocumentChunker chunker,
            PromptInjectionGuard guard,
            PiiRedactorPort piiRedactor) {
        return new IngestDocumentInteractor(chunkDataAccess, embeddingService, chunker, guard, piiRedactor);
    }

    @Bean
    @ConditionalOnProperty(name = "rag.storage.enabled", havingValue = "true")
    public MinioClient minioClient(
            @Value("${rag.storage.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${rag.storage.minio.access-key:minioadmin}") String accessKey,
            @Value("${rag.storage.minio.secret-key:minioadmin}") String secretKey) {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    @Bean
    public AskQuestionInputBoundary askQuestionInputBoundary(
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            GenerationServiceInterface generationService,
            PromptInjectionGuard guard,
            RrfFuser rrfFuser,
            RerankingServiceInterface rerankingService) {
        return new AskQuestionInteractor(chunkDataAccess, embeddingService, generationService, guard, rrfFuser, rerankingService);
    }

    @Bean
    public ListDocumentsInputBoundary listDocumentsInputBoundary(ChunkDataAccessInterface chunkDataAccess) {
        return new ListDocumentsInteractor(chunkDataAccess);
    }

    @Bean
    public DeleteDocumentInputBoundary deleteDocumentInputBoundary(ChunkDataAccessInterface chunkDataAccess) {
        return new DeleteDocumentInteractor(chunkDataAccess);
    }

    @Bean
    public SubmitIngestJobInputBoundary submitIngestJobInputBoundary(
            IngestJobRepositoryPort jobRepo,
            IngestJobDispatcherPort dispatcher) {
        return new SubmitIngestJobInteractor(jobRepo, dispatcher);
    }

    @Bean
    public GetIngestJobStatusInputBoundary getIngestJobStatusInputBoundary(IngestJobRepositoryPort jobRepo) {
        return new GetIngestJobStatusInteractor(jobRepo);
    }

    @Bean
    public RunEvaluationInputBoundary runEvaluationInputBoundary(
            EvaluationDatasetPort datasetPort,
            ChunkDataAccessInterface chunkDataAccess,
            EmbeddingServiceInterface embeddingService,
            RrfFuser rrfFuser,
            RerankingServiceInterface rerankingService) {
        return new RunEvaluationInteractor(datasetPort, chunkDataAccess, embeddingService, rrfFuser, rerankingService);
    }
}
