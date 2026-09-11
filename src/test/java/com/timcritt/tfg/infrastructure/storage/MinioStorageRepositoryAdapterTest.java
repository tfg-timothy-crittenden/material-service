package com.timcritt.tfg.infrastructure.storage;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MinioStorageRepositoryAdapterTest {

    @Test
    void deleteObject_whenMinioRemovalSucceeds_returnsNormally() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        MinioStorageRepositoryAdapter adapter = new MinioStorageRepositoryAdapter();
        setField(adapter, "minioClient", minioClient);

        assertThatCode(() -> adapter.deleteObject("toefl", "speaking/91/part1/audio/question_1/old.mp3"))
                .doesNotThrowAnyException();

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteObject_whenMinioRemovalFails_throwsWithOriginalFailureRetained() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        RuntimeException failure = new RuntimeException("minio down");
        doThrow(failure).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        MinioStorageRepositoryAdapter adapter = new MinioStorageRepositoryAdapter();
        setField(adapter, "minioClient", minioClient);

        assertThatThrownBy(() -> adapter.deleteObject("toefl", "speaking/91/part1/audio/question_1/old.mp3"))
                .isInstanceOf(RuntimeException.class)
                .satisfies(thrown -> assertThat(thrown == failure || thrown.getCause() == failure).isTrue());

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

