package dev.charlespaiva.rag.interfaceadapter.controller;

import dev.charlespaiva.rag.interfaceadapter.parser.CompositeDocumentTextExtractor;
import dev.charlespaiva.rag.interfaceadapter.presenter.AskQuestionPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.AskViewModel;
import dev.charlespaiva.rag.interfaceadapter.presenter.DeleteDocumentPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.DocumentListViewModel;
import dev.charlespaiva.rag.interfaceadapter.presenter.GetIngestJobStatusPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.IngestDocumentPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.IngestViewModel;
import dev.charlespaiva.rag.interfaceadapter.presenter.JobStatusViewModel;
import dev.charlespaiva.rag.interfaceadapter.presenter.ListDocumentsPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.SubmitIngestJobPresenter;
import dev.charlespaiva.rag.interfaceadapter.presenter.SubmitJobViewModel;
import dev.charlespaiva.rag.usecase.ask.AskQuestionInputBoundary;
import dev.charlespaiva.rag.usecase.ask.AskQuestionRequestModel;
import dev.charlespaiva.rag.usecase.documents.DeleteDocumentInputBoundary;
import dev.charlespaiva.rag.usecase.documents.DeleteDocumentRequestModel;
import dev.charlespaiva.rag.usecase.documents.ListDocumentsInputBoundary;
import dev.charlespaiva.rag.usecase.documents.ListDocumentsRequestModel;
import dev.charlespaiva.rag.usecase.ingest.GetIngestJobStatusInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentInputBoundary;
import dev.charlespaiva.rag.usecase.ingest.IngestDocumentRequestModel;
import dev.charlespaiva.rag.usecase.ingest.ObjectStoragePort;
import dev.charlespaiva.rag.usecase.ingest.SubmitIngestJobInputBoundary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "RAG", description = "Ingest documents and ask questions over private knowledge bases")
public class RagController {

    private final IngestDocumentInputBoundary ingestBoundary;
    private final AskQuestionInputBoundary askBoundary;
    private final ListDocumentsInputBoundary listDocumentsBoundary;
    private final DeleteDocumentInputBoundary deleteDocumentBoundary;
    private final CompositeDocumentTextExtractor compositeExtractor;
    private final SubmitIngestJobInputBoundary submitJobBoundary;
    private final GetIngestJobStatusInputBoundary getJobStatusBoundary;
    private final ObjectStoragePort objectStorage;

    public RagController(
            IngestDocumentInputBoundary ingestBoundary,
            AskQuestionInputBoundary askBoundary,
            ListDocumentsInputBoundary listDocumentsBoundary,
            DeleteDocumentInputBoundary deleteDocumentBoundary,
            CompositeDocumentTextExtractor compositeExtractor,
            SubmitIngestJobInputBoundary submitJobBoundary,
            GetIngestJobStatusInputBoundary getJobStatusBoundary,
            ObjectStoragePort objectStorage) {
        this.ingestBoundary = ingestBoundary;
        this.askBoundary = askBoundary;
        this.listDocumentsBoundary = listDocumentsBoundary;
        this.deleteDocumentBoundary = deleteDocumentBoundary;
        this.compositeExtractor = compositeExtractor;
        this.submitJobBoundary = submitJobBoundary;
        this.getJobStatusBoundary = getJobStatusBoundary;
        this.objectStorage = objectStorage;
    }

    @Operation(summary = "Ingest a document (plain text)",
        description = "Chunks, embeds, and stores a document for the given tenant.")
    @ApiResponse(responseCode = "201", description = "Document ingested; returns documentId and chunk count",
        content = @Content(schema = @Schema(implementation = IngestViewModel.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or content policy violation",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public IngestViewModel ingest(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @Valid @RequestBody IngestRequest r) {
        IngestDocumentPresenter presenter = new IngestDocumentPresenter();
        ingestBoundary.execute(new IngestDocumentRequestModel(tenantId, r.sourceName(), r.content()), presenter);
        return presenter.getViewModel();
    }

    @Operation(summary = "Ingest a file (PDF or DOCX)",
        description = "Extracts text from the uploaded file, then chunks, embeds, and stores it.")
    @ApiResponse(responseCode = "201", description = "File ingested; returns documentId and chunk count",
        content = @Content(schema = @Schema(implementation = IngestViewModel.class)))
    @ApiResponse(responseCode = "400", description = "Unsupported file type or content policy violation",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IngestViewModel ingestFile(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @RequestParam("sourceName") @NotBlank @Size(max = 255) String sourceName,
        @RequestPart("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String objectKey = objectStorage.store(tenantId, sourceName, bytes, file.getContentType());
        String content = compositeExtractor.extract(bytes, file.getContentType());
        IngestDocumentPresenter presenter = new IngestDocumentPresenter();
        ingestBoundary.execute(new IngestDocumentRequestModel(tenantId, sourceName, content), presenter);
        return new IngestViewModel(presenter.getViewModel().documentId(), presenter.getViewModel().chunks(), objectKey);
    }

    @Operation(summary = "Submit async ingest job",
        description = "Enqueues a document for background ingestion. Returns immediately with a job ID.")
    @ApiResponse(responseCode = "202", description = "Job accepted; poll /documents/jobs/{jobId} for status",
        content = @Content(schema = @Schema(implementation = SubmitJobViewModel.class)))
    @PostMapping("/documents/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitJobViewModel submitAsync(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @Valid @RequestBody IngestRequest r) {
        SubmitIngestJobPresenter presenter = new SubmitIngestJobPresenter();
        submitJobBoundary.execute(new IngestDocumentRequestModel(tenantId, r.sourceName(), r.content()), presenter);
        return new SubmitJobViewModel(presenter.getResponseModel().jobId(), presenter.getResponseModel().status().name());
    }

    @Operation(summary = "Get async ingest job status")
    @ApiResponse(responseCode = "200", description = "Job status",
        content = @Content(schema = @Schema(implementation = JobStatusViewModel.class)))
    @ApiResponse(responseCode = "404", description = "Job not found for this tenant")
    @GetMapping("/documents/jobs/{jobId}")
    public ResponseEntity<JobStatusViewModel> getJobStatus(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @PathVariable UUID jobId) {
        GetIngestJobStatusPresenter presenter = new GetIngestJobStatusPresenter();
        getJobStatusBoundary.execute(jobId, tenantId, presenter);
        if (!presenter.wasFound()) {
            return ResponseEntity.notFound().build();
        }
        return presenter.getResponseModel()
            .map(r -> ResponseEntity.ok(new JobStatusViewModel(r.jobId(), r.status().name(),
                r.createdAt().toString(), r.failureReason())))
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List documents",
        description = "Returns paginated ingested documents for the given tenant.")
    @ApiResponse(responseCode = "200", description = "Paginated list of ingested documents",
        content = @Content(schema = @Schema(implementation = DocumentListViewModel.class)))
    @GetMapping("/documents")
    public DocumentListViewModel listDocuments(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        ListDocumentsPresenter presenter = new ListDocumentsPresenter();
        listDocumentsBoundary.execute(new ListDocumentsRequestModel(tenantId, page, size), presenter);
        return presenter.getViewModel();
    }

    @Operation(summary = "Delete a document",
        description = "Deletes all chunks belonging to the given document for the given tenant.")
    @ApiResponse(responseCode = "204", description = "Document deleted")
    @ApiResponse(responseCode = "404", description = "Document not found for this tenant",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @PathVariable UUID documentId) {
        DeleteDocumentPresenter presenter = new DeleteDocumentPresenter();
        deleteDocumentBoundary.execute(new DeleteDocumentRequestModel(tenantId, documentId), presenter);
        if (presenter.wasFound()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Ask a question",
        description = "Runs hybrid (semantic + lexical) retrieval with RRF fusion and generates a grounded answer with citations.")
    @ApiResponse(responseCode = "200", description = "Answer with source citations",
        content = @Content(schema = @Schema(implementation = AskViewModel.class)))
    @ApiResponse(responseCode = "400", description = "Invalid question or security policy violation",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/questions")
    public AskViewModel ask(
        @Parameter(description = "Tenant identifier", required = true, example = "acme-corp")
        @RequestHeader("X-Tenant-Id") @NotBlank String tenantId,
        @Valid @RequestBody AskRequest r) {
        AskQuestionPresenter presenter = new AskQuestionPresenter();
        askBoundary.execute(new AskQuestionRequestModel(tenantId, r.question(), r.topK()), presenter);
        return presenter.getViewModel();
    }

    public record IngestRequest(
        @Schema(description = "Document source name", example = "handbook-2024.pdf")
        @NotBlank @Size(max = 255) String sourceName,
        @Schema(description = "Plain-text document content (max 2 MB)")
        @NotBlank @Size(max = 2_000_000) String content) {
    }

    public record AskRequest(
        @Schema(description = "Natural-language question", example = "What is the vacation policy?")
        @NotBlank @Size(max = 4000) String question,
        @Schema(description = "Number of chunks to retrieve (1–20); defaults to 5", minimum = "1", maximum = "20")
        @Min(1) @Max(20) int topK) {
        public AskRequest {
            if (topK == 0) {
                topK = 5;
            }
        }
    }
}
