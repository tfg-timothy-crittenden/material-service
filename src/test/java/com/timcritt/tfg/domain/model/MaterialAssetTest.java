package com.timcritt.tfg.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaterialAssetTest {

    private static final OffsetDateTime ORIGINAL_TIME = OffsetDateTime.parse("2020-01-01T00:00:00Z");

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {5L})
    void replaceFile_normalizesMetadataAndVersionsOnce_preservingIdentityAndOtherState(Long version) {
        MaterialAsset asset = asset(version);

        asset.replaceFile("  new-key  ", "  new.mp3  ", "  audio/mpeg  ", 1234L);

        assertThat(asset.getStorageKey()).isEqualTo("new-key");
        assertThat(asset.getOriginalFilename()).isEqualTo("new.mp3");
        assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
        assertThat(asset.getFileSizeBytes()).isEqualTo(1234L);
        assertThat(asset.getVersion()).isEqualTo(version == null ? 1L : version + 1L);
        assertThat(asset.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertIdentityAndNonFileState(asset);
    }

    @Test
    void replaceFile_identicalKeyAndAllMetadata_isStillAnExplicitReplacement() {
        MaterialAsset asset = asset(5L);

        asset.replaceFile("key-X", "old.wav", "audio/wav", 123L);

        assertOriginalFileMetadata(asset);
        assertThat(asset.getVersion()).isEqualTo(6L);
        assertThat(asset.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertIdentityAndNonFileState(asset);
    }

    @ParameterizedTest
    @MethodSource("invalidFiles")
    void replaceFile_invalidInput_doesNotPartiallyMutate(String storageKey, Long size, String message) {
        MaterialAsset asset = asset(5L);

        assertThatThrownBy(() -> asset.replaceFile(storageKey, "new.mp3", "audio/mpeg", size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);

        assertOriginalFileMetadata(asset);
        assertThat(asset.getVersion()).isEqualTo(5L);
        assertThat(asset.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        assertIdentityAndNonFileState(asset);
    }

    private static Stream<Arguments> invalidFiles() {
        return Stream.of(
                Arguments.of(null, 1234L, "storageKey cannot be blank"),
                Arguments.of("  ", 1234L, "storageKey cannot be blank"),
                Arguments.of("new-key", -1L, "fileSizeBytes cannot be negative")
        );
    }

    private static MaterialAsset asset(Long version) {
        return MaterialAsset.builder()
                .id(700L).materialNodeId(1003L).kind(MaterialAsset.Kind.AUDIO)
                .storageKey("key-X").originalFilename("old.wav").mimeType("audio/wav").fileSizeBytes(123L)
                .title("Asset title").transcriptText("Asset transcript").displayOrder(2)
                .metadata(Map.of("language", "en")).version(version)
                .createdAt(ORIGINAL_TIME).updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static void assertOriginalFileMetadata(MaterialAsset asset) {
        assertThat(asset.getStorageKey()).isEqualTo("key-X");
        assertThat(asset.getOriginalFilename()).isEqualTo("old.wav");
        assertThat(asset.getMimeType()).isEqualTo("audio/wav");
        assertThat(asset.getFileSizeBytes()).isEqualTo(123L);
    }

    private static void assertIdentityAndNonFileState(MaterialAsset asset) {
        assertThat(asset.getId()).isEqualTo(700L);
        assertThat(asset.getMaterialNodeId()).isEqualTo(1003L);
        assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
        assertThat(asset.getTitle()).isEqualTo("Asset title");
        assertThat(asset.getTranscriptText()).isEqualTo("Asset transcript");
        assertThat(asset.getDisplayOrder()).isEqualTo(2);
        assertThat(asset.getMetadata()).containsExactlyEntriesOf(Map.of("language", "en"));
        assertThat(asset.getCreatedAt()).isEqualTo(ORIGINAL_TIME);
    }
}

