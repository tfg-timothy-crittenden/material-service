package com.timcritt.tfg.application.port.outbound;

import java.io.InputStream;

/**
 * Port for storage operations (e.g., MinIO, S3).
 */
public interface StorageRepositoryPort {

    /**
     * Generates a presigned URL for accessing an object.
     * @param bucket the bucket name
     * @param objectKey the object key
     * @param expirationSeconds expiration in seconds
     * @return the presigned URL
     */
    String generatePresignedUrl(String bucket, String objectKey, long expirationSeconds);

    /**
     * Uploads an object to storage.
     * @param bucket the bucket name
     * @param objectKey the object key
     * @param inputStream the input stream of the object
     */
    void uploadObject(String bucket, String objectKey, InputStream inputStream);

    /**
     * Deletes an object from storage.
     * @param bucket the bucket name
     * @param objectKey the object key
     * @return true if deleted, false otherwise
     */
    boolean deleteObject(String bucket, String objectKey);
}
