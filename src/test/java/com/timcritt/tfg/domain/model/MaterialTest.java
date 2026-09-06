package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.policy.MaterialPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MaterialTest {

    @Test
    void publish_draftMaterial_validatesAndTransitionsToPublished() {
        Material material = Material.builder()
                .id(10001L)
                .title("TOEFL Speaking Test 1")
                .status(MaterialStatus.DRAFT)
                .version(7L)
                .updatedAt(Instant.parse("2026-09-06T10:00:00Z"))
                .build();

        MaterialPolicy policy = mock(MaterialPolicy.class);

        material.publish(policy);

        verify(policy, times(1)).validateForPublication(material);
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.PUBLISHED);
        assertThat(material.getVersion()).isEqualTo(8L);
        assertThat(material.getUpdatedAt()).isAfter(Instant.parse("2026-09-06T10:00:00Z"));
    }

    @Test
    void publish_alreadyPublishedMaterial_isNoOp() {
        Instant updatedAt = Instant.parse("2026-09-06T10:00:00Z");
        Material material = Material.builder()
                .id(10001L)
                .title("TOEFL Speaking Test 1")
                .status(MaterialStatus.PUBLISHED)
                .version(7L)
                .updatedAt(updatedAt)
                .build();

        MaterialPolicy policy = mock(MaterialPolicy.class);

        material.publish(policy);

        verify(policy, never()).validateForPublication(material);
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.PUBLISHED);
        assertThat(material.getVersion()).isEqualTo(7L);
        assertThat(material.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void publish_nonDraftMaterial_throwsAndDoesNotValidate() {
        Material material = Material.builder()
                .id(10001L)
                .title("TOEFL Speaking Test 1")
                .status(MaterialStatus.ARCHIVED)
                .version(7L)
                .build();

        MaterialPolicy policy = mock(MaterialPolicy.class);

        assertThatThrownBy(() -> material.publish(policy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only draft materials can be published");

        verify(policy, never()).validateForPublication(material);
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.ARCHIVED);
        assertThat(material.getVersion()).isEqualTo(7L);
    }
}
