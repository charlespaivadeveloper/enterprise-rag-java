package dev.charlespaiva.rag.usecase.ingest;

public interface DocumentTextExtractorPort {
    boolean supports(String mediaType);
    String extract(byte[] content, String mediaType);
}
