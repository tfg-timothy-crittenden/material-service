package com.timcritt.tfg.infrastructure.storage;

import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;


@Component
public class MinioStorageRepositoryAdapter implements StorageRepositoryPort {

    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.public-url:#{null}}")
    private String minioPublicUrl;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.region:us-east-1}")
    private String region;

    private MinioClient minioClient;
    private MinioClient presignMinioClient;

    @PostConstruct
    public void init() {
        // Internal endpoint used by the service container for upload/delete operations.
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();

        // Presigned URLs must be signed with the same host clients will call (host is part of SigV4).
        // We therefore sign against minio.public-url when set, and fall back to minio.url otherwise.
        String presignEndpoint = (minioPublicUrl != null && !minioPublicUrl.isBlank()) ? minioPublicUrl : minioUrl;
        this.presignMinioClient = MinioClient.builder()
                .endpoint(presignEndpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }

    @Override
    public String generatePresignedUrl(String bucket, String objectKey, long expirationSeconds) {
        try {
            return presignMinioClient.getPresignedObjectUrl(
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
            ensureBucketExists(bucket);
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

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure bucket exists: " + bucket, e);
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
