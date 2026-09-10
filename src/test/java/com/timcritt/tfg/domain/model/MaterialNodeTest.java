package com.timcritt.tfg.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaterialNodeTest {

    private static final Instant ORIGINAL_TIME = Instant.parse("2020-01-01T00:00:00Z");

    @Test
    void childAt_childrenAddedOutOfOrder_returnsChildByDisplayOrderNotListPosition() {
        MaterialNode parent = node(100L, null, 0);
        MaterialNode orderThree = node(101L, parent.getId(), 3);
        MaterialNode orderZero = node(102L, parent.getId(), 0);
        parent.addChild(orderThree);
        parent.addChild(orderZero);

        assertThat(parent.childAt(0)).isSameAs(orderZero);
        assertThat(parent.childAt(3)).isSameAs(orderThree);
        assertThat(parent.getChildren()).containsExactly(orderThree, orderZero);
    }

    @Test
    void childAt_missingDisplayOrder_throwsClearException() {
        MaterialNode parent = node(100L, null, 0);
        parent.addChild(node(101L, parent.getId(), 3));

        assertThatThrownBy(() -> parent.childAt(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No child with displayOrder 0 for node 100");
    }

    @Test
    void childAt_noChildren_throwsClearException() {
        MaterialNode parent = node(100L, null, 0);

        assertThatThrownBy(() -> parent.childAt(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No child with displayOrder 0 for node 100");
    }

    @Test
    void childAt_lookup_keepsChildrenCollectionUnmodifiable() {
        MaterialNode parent = node(100L, null, 0);
        MaterialNode child = node(101L, parent.getId(), 3);
        parent.addChild(child);

        assertThat(parent.childAt(3)).isSameAs(child);
        assertThatThrownBy(() -> parent.getChildren().add(node(102L, parent.getId(), 0)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> parent.getChildren().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(parent.getChildren()).containsExactly(child);
    }

    @Test
    void updateTitle_normalizesAndVersionsChange_normalizedEqualTitleIsNoOp() {
        MaterialNode node = mutableNode(Map.of());

        node.updateTitle("  Revised title  ");

        assertThat(node.getTitle()).isEqualTo("Revised title");
        assertChangedOnce(node);
        Instant updatedAt = node.getUpdatedAt();

        node.updateTitle("Revised title");
        node.updateTitle("  Revised title  ");

        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void updateTranscriptText_normalizesAndVersionsChange_normalizedEqualTextIsNoOp() {
        MaterialNode node = mutableNode(Map.of());

        node.updateTranscriptText("  Revised transcript  ");

        assertThat(node.getTranscriptText()).isEqualTo("Revised transcript");
        assertChangedOnce(node);
        Instant updatedAt = node.getUpdatedAt();

        node.updateTranscriptText("Revised transcript");
        node.updateTranscriptText("  Revised transcript  ");

        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void updateConfig_copiesAndVersionsChange_equalDistinctMapIsNoOp() {
        MaterialNode node = mutableNode(Map.of());
        Map<String, Object> config = new HashMap<>(Map.of("durationSeconds", 60));

        node.updateConfig(config);
        config.put("durationSeconds", 90);

        assertThat(node.getConfig()).containsExactlyEntriesOf(Map.of("durationSeconds", 60));
        assertChangedOnce(node);
        Instant updatedAt = node.getUpdatedAt();

        node.updateConfig(new HashMap<>(Map.of("durationSeconds", 60)));

        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void updateConfig_storedNullToEmpty_isChangeEvenThoughGetterWasAlreadyEmpty() {
        MaterialNode node = mutableNode(null);
        assertThat(node.getConfig()).isEmpty();

        node.updateConfig(Map.of());

        assertThat(node.getConfig()).isEmpty();
        assertChangedOnce(node);
        Instant updatedAt = node.getUpdatedAt();

        node.updateConfig(null);
        node.updateConfig(Map.of());

        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private static MaterialNode mutableNode(Map<String, Object> config) {
        return MaterialNode.builder()
                .id(101L)
                .materialId(1L)
                .title("Original title")
                .transcriptText("Original transcript")
                .config(config)
                .version(3L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static void assertChangedOnce(MaterialNode node) {
        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(node.getCreatedAt()).isEqualTo(ORIGINAL_TIME);
    }

    private static MaterialNode node(Long id, Long parentNodeId, int displayOrder) {
        return MaterialNode.builder()
                .id(id)
                .materialId(1L)
                .parentNodeId(parentNodeId)
                .displayOrder(displayOrder)
                .build();
    }
}

