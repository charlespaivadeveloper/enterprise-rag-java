package dev.charlespaiva.rag.interfaceadapter.storage;

import dev.charlespaiva.rag.usecase.ingest.ObjectStoragePort;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "rag.storage.enabled", havingValue = "true")
public class MinioObjectStorageAdapter implements ObjectStoragePort {

    private final MinioClient minio;
    private final String bucket;

    public MinioObjectStorageAdapter(MinioClient minio,
                                     @Value("${rag.storage.minio.bucket:rag-documents}") String bucket) {
        this.minio = minio;
        this.bucket = bucket;
    }

    @Override
    public String store(String tenantId, String sourceName, byte[] content, String mediaType) {
        String objectKey = tenantId + "/" + UUID.randomUUID() + "/" + sourceName;
        try {
            ensureBucket();
            minio.putObject(PutObjectArgs.builder()
                .bucket(bucket).object(objectKey)
                .stream(new ByteArrayInputStream(content), content.length, -1)
                .contentType(mediaType).build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO store failed for " + objectKey, e);
        }
        return objectKey;
    }

    @Override
    public byte[] retrieve(String objectKey) {
        try {
            return minio.getObject(GetObjectArgs.builder()
                .bucket(bucket).object(objectKey).build()).readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("MinIO retrieve failed for " + objectKey, e);
        }
    }

    private void ensureBucket() {
        try {
            if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO bucket check failed: " + bucket, e);
        }
    }
}
