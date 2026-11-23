package dev.charlespaiva.rag.usecase.ingest;

public interface ObjectStoragePort {
    String store(String tenantId, String sourceName, byte[] content, String mediaType);
    byte[] retrieve(String objectKey);
}
