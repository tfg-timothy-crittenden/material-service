package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.policy.MaterialPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MaterialTest {

    private static final Instant ORIGINAL_TIME = Instant.parse("2020-01-01T00:00:00Z");

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

    @Test
    void updateNodeTitle_attachedChild_changesNodeAndVersionsAggregateOnce() {
        Tree tree = attachedTree();

        tree.material().updateNodeTitle(tree.child().getId(), "  Revised title  ");

        assertThat(tree.child().getTitle()).isEqualTo("Revised title");
        assertChangedOnce(tree.material(), tree.child());
        assertNodeUnchanged(tree.root());
        assertNodeUnchanged(tree.question());
        assertNodeUnchanged(tree.sibling());
    }

    @Test
    void updateNodeTranscript_nestedQuestion_changesNodeAndVersionsAggregateOnce() {
        Tree tree = attachedTree();

        tree.material().updateNodeTranscript(tree.question().getId(), "  Revised transcript  ");

        assertThat(tree.question().getTranscriptText()).isEqualTo("Revised transcript");
        assertChangedOnce(tree.material(), tree.question());
        assertNodeUnchanged(tree.root());
        assertNodeUnchanged(tree.child());
        assertNodeUnchanged(tree.sibling());
    }

    @Test
    void updateNodeConfig_nestedQuestion_copiesConfigAndVersionsAggregateOnce() {
        Tree tree = attachedTree();
        Map<String, Object> config = new HashMap<>(Map.of("durationSeconds", 60));

        tree.material().updateNodeConfig(tree.question().getId(), config);
        config.put("durationSeconds", 90);

        assertThat(tree.question().getConfig()).containsExactlyEntriesOf(Map.of("durationSeconds", 60));
        assertChangedOnce(tree.material(), tree.question());
        assertNodeUnchanged(tree.root());
        assertNodeUnchanged(tree.child());
        assertNodeUnchanged(tree.sibling());
    }

    // The former independent root-title case is superseded by the updateDetails rename tests.
    // Root-target updateNodeTitle semantics are deferred; transcript/config coverage remains.
    @ParameterizedTest(name = "{0}: attached root is a valid content target")
    @MethodSource("nodeContentMutations")
    void updateNodeContent_attachedRoot_changesRootAndVersionsAggregateOnce(
            String operation, BiConsumer<Material, Long> mutation) {
        Tree tree = attachedTree();

        mutation.accept(tree.material(), tree.root().getId());

        assertChangedOnce(tree.material(), tree.root());
        assertNodeUnchanged(tree.child());
        assertNodeUnchanged(tree.question());
        assertNodeUnchanged(tree.sibling());
    }

    @Test
    void updateNodeTitle_identicalOrNormalizedEqualTitle_preservesBothVersionsAndTimestamps() {
        Tree tree = attachedTree();

        tree.material().updateNodeTitle(tree.child().getId(), "Original title");
        assertTreeUnchanged(tree);

        tree.material().updateNodeTitle(tree.child().getId(), "  Original title  ");
        assertTreeUnchanged(tree);
    }

    @Test
    void updateNodeTranscript_identicalOrNormalizedEqualText_preservesBothVersionsAndTimestamps() {
        Tree tree = attachedTree();

        tree.material().updateNodeTranscript(tree.question().getId(), "Original transcript");
        assertTreeUnchanged(tree);

        tree.material().updateNodeTranscript(tree.question().getId(), "  Original transcript  ");
        assertTreeUnchanged(tree);
    }

    @Test
    void updateNodeConfig_equalDistinctMap_preservesBothVersionsAndTimestamps() {
        Tree tree = attachedTree();
        Map<String, Object> equalConfig = new HashMap<>(tree.question().getConfig());

        tree.material().updateNodeConfig(tree.question().getId(), equalConfig);

        assertTreeUnchanged(tree);
    }

    @Test
    void updateNodeTranscript_nullClearsText_repeatedNullIsNoOp() {
        Tree tree = attachedTree();

        tree.material().updateNodeTranscript(tree.question().getId(), null);

        assertThat(tree.question().getTranscriptText()).isNull();
        assertChangedOnce(tree.material(), tree.question());
        Instant materialUpdatedAt = tree.material().getUpdatedAt();
        Instant nodeUpdatedAt = tree.question().getUpdatedAt();

        tree.material().updateNodeTranscript(tree.question().getId(), null);

        assertThat(tree.material().getVersion()).isEqualTo(8L);
        assertThat(tree.question().getVersion()).isEqualTo(4L);
        assertThat(tree.material().getUpdatedAt()).isEqualTo(materialUpdatedAt);
        assertThat(tree.question().getUpdatedAt()).isEqualTo(nodeUpdatedAt);
    }

    @Test
    void updateNodeConfig_nullClearsConfig_repeatedNullIsNoOp() {
        Tree tree = attachedTree();

        tree.material().updateNodeConfig(tree.question().getId(), null);

        assertThat(tree.question().getConfig()).isEmpty();
        assertChangedOnce(tree.material(), tree.question());
        Instant materialUpdatedAt = tree.material().getUpdatedAt();
        Instant nodeUpdatedAt = tree.question().getUpdatedAt();

        tree.material().updateNodeConfig(tree.question().getId(), null);

        assertThat(tree.material().getVersion()).isEqualTo(8L);
        assertThat(tree.question().getVersion()).isEqualTo(4L);
        assertThat(tree.material().getUpdatedAt()).isEqualTo(materialUpdatedAt);
        assertThat(tree.question().getUpdatedAt()).isEqualTo(nodeUpdatedAt);
    }

    @Test
    void updateNodeConfig_storedNullToEmpty_tracksExistingNodeMutationDespiteEqualGetterValues() {
        Tree tree = attachedTree();
        MaterialNode question = node(1005L, tree.child().getId(), MaterialNodeKind.ITEM, null);
        tree.child().addChild(question);
        assertThat(question.getConfig()).isEmpty();

        tree.material().updateNodeConfig(question.getId(), Map.of());

        assertThat(question.getConfig()).isEmpty();
        assertChangedOnce(tree.material(), question);
    }

    @Test
    void updateNodeTitle_blankTitle_preservesExistingValidationWithoutVersioning() {
        Tree tree = attachedTree();

        assertThatThrownBy(() -> tree.material().updateNodeTitle(tree.child().getId(), "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title cannot be blank");

        assertTreeUnchanged(tree);
    }

    @ParameterizedTest(name = "{0}: missing node ID is rejected")
    @MethodSource("nodeMutations")
    void updateNode_missingId_throwsClearExceptionWithoutMutation(
            String operation, BiConsumer<Material, Long> mutation) {
        Tree tree = attachedTree();

        assertThatThrownBy(() -> mutation.accept(tree.material(), 9999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("9999");

        assertTreeUnchanged(tree);
    }

    @ParameterizedTest(name = "{0}: matching material and parent IDs do not imply attachment")
    @MethodSource("nodeMutations")
    void updateNode_detachedNodeWithMatchingMaterialId_cannotBeMutatedThroughMaterial(
            String operation, BiConsumer<Material, Long> mutation) {
        Tree tree = attachedTree();
        MaterialNode detached = node(2001L, tree.child().getId(), MaterialNodeKind.ITEM,
                Map.of("durationSeconds", 30));
        assertThat(detached.getMaterialId()).isEqualTo(tree.material().getId());
        assertThat(tree.child().getChildren()).doesNotContain(detached);

        assertThatThrownBy(() -> mutation.accept(tree.material(), detached.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(detached.getId().toString());

        assertNodeUnchanged(detached);
        assertTreeUnchanged(tree);
    }

    @ParameterizedTest(name = "{0}: an aggregate without an attached root has no target")
    @MethodSource("nodeMutations")
    void updateNode_noAttachedRoot_throwsWithoutVersioning(
            String operation, BiConsumer<Material, Long> mutation) {
        Material material = material();

        assertThatThrownBy(() -> mutation.accept(material, 1001L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1001");

        assertThat(material.getVersion()).isEqualTo(7L);
        assertThat(material.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
    }

    @ParameterizedTest
    @ValueSource(strings = {"New Title", "  New Title  "})
    void updateDetails_rename_synchronizesRootAndVersionsAggregateOnce(String title) {
        Material material = detailsMaterial("Old", "Old");

        material.updateDetails(title, null);

        assertDetailsState(material, "New Title", "Material description", "New Title", true, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Same", "  Same  "})
    void updateDetails_explicitSameTitle_repairsDriftAndCountsAsChange(String title) {
        Material material = detailsMaterial("Same", "Stale root title");

        material.updateDetails(title, null);

        assertDetailsState(material, "Same", "Material description", "Same", true, true);
    }

    @ParameterizedTest
    @MethodSource("unchangedDetails")
    void updateDetails_equalOrOmittedValues_preserveAggregateAndRootMetadata(String title, String description) {
        Material material = detailsMaterial("Same", "Same");

        material.updateDetails(title, description);

        assertDetailsState(material, "Same", "Material description", "Same", false, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Same", "Stale root title"})
    void updateDetails_descriptionOnly_versionsOnceAndLeavesRootUntouched(String rootTitle) {
        Material material = detailsMaterial("Same", rootTitle);

        material.updateDetails(null, "  New description  ");

        assertDetailsState(material, "Same", "New description", rootTitle, true, false);
    }

    @Test
    void updateDetails_titleAndDescriptionChange_synchronizesRootAndVersionsAggregateOnlyOnce() {
        Material material = detailsMaterial("Old", "Old");

        material.updateDetails("  New Title  ", "  New description  ");

        assertDetailsState(material, "New Title", "New description", "New Title", true, true);
    }

    @Test
    void updateDetails_rootAlreadyHasRequestedTitle_preservesRootVersionAndTimestamp() {
        Material material = detailsMaterial("Old", "New Title");

        material.updateDetails("New Title", null);

        assertDetailsState(material, "New Title", "Material description", "New Title", true, false);
    }

    @ParameterizedTest
    @MethodSource("rootlessDetailsChanges")
    void updateDetails_withoutRoot_changesDetailsAndVersionsOnce(
            String title, String description, String expectedTitle, String expectedDescription) {
        Material material = detailsMaterial("Same");

        material.updateDetails(title, description);

        assertDetailsState(material, expectedTitle, expectedDescription, null, true, false);
    }

    @ParameterizedTest
    @MethodSource("unchangedDetails")
    void updateDetails_withoutRoot_equalOrOmittedValuesPreserveMetadata(String title, String description) {
        Material material = detailsMaterial("Same");

        material.updateDetails(title, description);

        assertDetailsState(material, "Same", "Material description", null, false, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void updateDetails_blankTitle_rejectsBeforeChangingAnyState(String title) {
        Material material = detailsMaterial("Same", "Same");

        assertThatThrownBy(() -> material.updateDetails(title, "New description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title cannot be blank");

        assertDetailsState(material, "Same", "Material description", "Same", false, false);
    }

    @Test
    void updateDetails_nullArguments_omitBothFieldsAndDoNotRepairDrift() {
        Material material = detailsMaterial("Same", "Stale root title");

        material.updateDetails(null, null);

        assertDetailsState(material, "Same", "Material description", "Stale root title", false, false);
    }

    private static Stream<Arguments> unchangedDetails() {
        return Stream.of(
                Arguments.of("Same", null),
                Arguments.of("  Same  ", null),
                Arguments.of(null, "Material description"),
                Arguments.of(null, "  Material description  "),
                Arguments.of("  Same  ", "  Material description  "),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> rootlessDetailsChanges() {
        return Stream.of(
                Arguments.of("  New Title  ", null, "New Title", "Material description"),
                Arguments.of(null, "  New description  ", "Same", "New description"),
                Arguments.of("New Title", "New description", "New Title", "New description")
        );
    }

    private static Material detailsMaterial(String title) {
        return Material.builder()
                .id(10001L)
                .title(title)
                .description("Material description")
                .status(MaterialStatus.DRAFT)
                .version(7L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static Material detailsMaterial(String title, String rootTitle) {
        Material material = detailsMaterial(title);
        MaterialNode root = MaterialNode.builder()
                .id(1001L)
                .materialId(material.getId())
                .kind(MaterialNodeKind.SECTION)
                .title(rootTitle)
                .transcriptText("Original transcript")
                .config(Map.of("durationSeconds", 30))
                .version(3L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
        root.addChild(node(1002L, root.getId(), MaterialNodeKind.PART, Map.of("durationSeconds", 30)));
        // Deliberately permit stale rehydrated state here: attachment is outside this contract.
        material.attachRoot(root);
        return material;
    }

    private static void assertDetailsState(
            Material material, String title, String description, String rootTitle,
            boolean materialChanged, boolean rootChanged) {
        assertAll(
                () -> assertThat(material.getTitle()).as("material title").isEqualTo(title),
                () -> assertThat(material.getDescription()).as("material description").isEqualTo(description),
                () -> assertThat(material.getVersion()).as("material version").isEqualTo(materialChanged ? 8L : 7L),
                () -> {
                    if (materialChanged) {
                        assertThat(material.getUpdatedAt()).as("material updatedAt").isAfter(ORIGINAL_TIME);
                    } else {
                        assertThat(material.getUpdatedAt()).as("material updatedAt").isEqualTo(ORIGINAL_TIME);
                    }
                },
                () -> assertThat(material.getCreatedAt()).isEqualTo(ORIGINAL_TIME),
                () -> assertThat(material.getStatus()).isEqualTo(MaterialStatus.DRAFT),
                () -> {
                    if (rootTitle == null) {
                        assertThat(material.hasRoot()).isFalse();
                        assertThat(material.getRoot()).isNull();
                        return;
                    }
                    MaterialNode root = material.getRoot();
                    assertThat(root).isNotNull();
                    assertAll(
                            () -> assertThat(root.getTitle()).as("root title").isEqualTo(rootTitle),
                            () -> assertThat(root.getVersion()).as("root version").isEqualTo(rootChanged ? 4L : 3L),
                            () -> {
                                if (rootChanged) {
                                    assertThat(root.getUpdatedAt()).as("root updatedAt").isAfter(ORIGINAL_TIME);
                                } else {
                                    assertThat(root.getUpdatedAt()).as("root updatedAt").isEqualTo(ORIGINAL_TIME);
                                }
                            },
                            () -> assertThat(root.getCreatedAt()).isEqualTo(ORIGINAL_TIME),
                            () -> assertThat(root.getTranscriptText()).isEqualTo("Original transcript"),
                            () -> assertThat(root.getConfig()).containsExactlyEntriesOf(Map.of("durationSeconds", 30)),
                            () -> {
                                assertThat(root.getChildren()).hasSize(1);
                                assertNodeUnchanged(root.getChildren().getFirst());
                            }
                    );
                }
        );
    }

    private static Stream<Arguments> nodeMutations() {
        return Stream.concat(
                Stream.of(Arguments.of("title", (BiConsumer<Material, Long>)
                        (material, id) -> material.updateNodeTitle(id, "Revised title"))),
                nodeContentMutations()
        );
    }

    private static Stream<Arguments> nodeContentMutations() {
        return Stream.of(
                Arguments.of("transcript", (BiConsumer<Material, Long>)
                        (material, id) -> material.updateNodeTranscript(id, "Revised transcript")),
                Arguments.of("config", (BiConsumer<Material, Long>)
                        (material, id) -> material.updateNodeConfig(id, Map.of("durationSeconds", 60)))
        );
    }

    private static Tree attachedTree() {
        Material material = material();
        MaterialNode root = node(1001L, null, MaterialNodeKind.SECTION, Map.of("durationSeconds", 30));
        MaterialNode child = node(1002L, root.getId(), MaterialNodeKind.PART, Map.of("durationSeconds", 30));
        MaterialNode question = node(1003L, child.getId(), MaterialNodeKind.ITEM, Map.of("durationSeconds", 30));
        MaterialNode sibling = node(1004L, root.getId(), MaterialNodeKind.PART, Map.of("durationSeconds", 30));
        // Put another branch first: lookup must search descendants, not assume a list position.
        root.addChild(sibling);
        root.addChild(child);
        child.addChild(question);
        material.attachRoot(root);
        return new Tree(material, root, child, question, sibling);
    }

    private static Material material() {
        return Material.builder()
                .id(10001L)
                .title("Material title")
                .description("Material description")
                .status(MaterialStatus.DRAFT)
                .version(7L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static MaterialNode node(Long id, Long parentId, MaterialNodeKind kind, Map<String, Object> config) {
        return MaterialNode.builder()
                .id(id)
                .materialId(10001L)
                .parentNodeId(parentId)
                .kind(kind)
                .title("Original title")
                .transcriptText("Original transcript")
                .config(config)
                .version(3L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static void assertChangedOnce(Material material, MaterialNode node) {
        assertThat(node.getVersion()).isEqualTo(4L);
        assertThat(node.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(node.getCreatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(material.getVersion()).isEqualTo(8L);
        assertThat(material.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(material.getCreatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(material.getTitle()).isEqualTo("Material title");
        assertThat(material.getDescription()).isEqualTo("Material description");
        assertThat(material.getStatus()).isEqualTo(MaterialStatus.DRAFT);
    }

    private static void assertTreeUnchanged(Tree tree) {
        assertThat(tree.material().getVersion()).isEqualTo(7L);
        assertThat(tree.material().getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(tree.material().getCreatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(tree.material().getTitle()).isEqualTo("Material title");
        assertThat(tree.material().getDescription()).isEqualTo("Material description");
        assertNodeUnchanged(tree.root());
        assertNodeUnchanged(tree.child());
        assertNodeUnchanged(tree.question());
        assertNodeUnchanged(tree.sibling());
    }

    private static void assertNodeUnchanged(MaterialNode node) {
        assertThat(node.getTitle()).isEqualTo("Original title");
        assertThat(node.getTranscriptText()).isEqualTo("Original transcript");
        assertThat(node.getConfig()).containsExactlyEntriesOf(Map.of("durationSeconds", 30));
        assertThat(node.getVersion()).isEqualTo(3L);
        assertThat(node.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(node.getCreatedAt()).isEqualTo(ORIGINAL_TIME);
    }

    private record Tree(Material material, MaterialNode root, MaterialNode child,
                        MaterialNode question, MaterialNode sibling) {
    }
}
