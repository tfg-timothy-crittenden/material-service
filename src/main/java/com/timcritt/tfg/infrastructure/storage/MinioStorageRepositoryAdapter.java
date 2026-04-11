package com.timcritt.tfg.infrastructure.storage;

import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Adapter for MinIO storage operations.
 */
@Component
public class MinioStorageRepositoryAdapter implements StorageRepositoryPort {

    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.region:us-east-1}")
    private String region;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }

    @Override
    public String generatePresignedUrl(String bucket, String objectKey, long expirationSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) expirationSeconds)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public void uploadObject(String bucket, String objectKey, InputStream inputStream) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, -1, 10485760)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload object", e);
        }
    }

    @Override
    public boolean deleteObject(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
