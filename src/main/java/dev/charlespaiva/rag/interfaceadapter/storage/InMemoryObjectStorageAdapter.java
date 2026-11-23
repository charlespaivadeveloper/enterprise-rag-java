package dev.charlespaiva.rag.interfaceadapter.storage;

import dev.charlespaiva.rag.usecase.ingest.ObjectStoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "rag.storage.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryObjectStorageAdapter implements ObjectStoragePort {

    private final Map<String, byte[]> objects = new ConcurrentHashMap<>();

    @Override
    public String store(String tenantId, String sourceName, byte[] content, String mediaType) {
        String objectKey = tenantId + "/" + UUID.randomUUID() + "/" + sourceName;
        objects.put(objectKey, content.clone());
        return objectKey;
    }

    @Override
    public byte[] retrieve(String objectKey) {
        byte[] bytes = objects.get(objectKey);
        if (bytes == null) {
            throw new NoSuchElementException("Object not found: " + objectKey);
        }
        return bytes.clone();
    }
}
