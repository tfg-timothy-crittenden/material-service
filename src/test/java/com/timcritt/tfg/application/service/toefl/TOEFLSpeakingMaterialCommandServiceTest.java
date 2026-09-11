package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageCleanupPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DELETED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TOEFLSpeakingMaterialCommandServiceTest {

    private static final Instant ORIGINAL_TIME = Instant.parse("2020-01-01T00:00:00Z");

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final StorageRepositoryPort storageRepositoryPort = mock(StorageRepositoryPort.class);
    private final StorageCleanupPort storageCleanupPort = mock(StorageCleanupPort.class);
    private final IntegrationEventOutboxPort outboxPort = mock(IntegrationEventOutboxPort.class);
    private final Supplier<UUID> replacementKeyUuidSupplier = new TestUuidSequenceSupplier(
            "550e8400-e29b-41d4-a716-446655440000",
            "550e8400-e29b-41d4-a716-446655440001",
            "550e8400-e29b-41d4-a716-446655440002",
            "550e8400-e29b-41d4-a716-446655440003"
    );

    private final TOEFLSpeakingMaterialCommandService service = new TOEFLSpeakingMaterialCommandService(
            materialRepository,
            storageRepositoryPort,
            storageCleanupPort,
            outboxPort,
            replacementKeyUuidSupplier
    );

    @Test
    void uploadSpeakingSection_scaffoldsMissingDraftQuestionNodesForBothParts() {
        Material persistedScaffold = persistedSpeakingScaffoldMaterial(1000L, "Draft section", null);
        Material finalAggregate = persistedSpeakingScaffoldMaterial(1000L, "Draft section", null);
        finalAggregate.addNodeAsset(9101L, MaterialAsset.Kind.IMAGE,
                "speaking/1000/part1/image/image.png", "cover.png", "image/png", 3L);
        finalAggregate.addNodeAsset(9110L, MaterialAsset.Kind.AUDIO,
                "speaking/1000/part1/audio/question_1.mp3", "question-audio.mp3", "audio/mpeg", 3L);
        finalAggregate.addNodeAsset(9120L, MaterialAsset.Kind.AUDIO,
                "speaking/1000/part2/audio/question_1.mp3", "question-audio.mp3", "audio/mpeg", 3L);
        finalAggregate.addNodeAsset(9121L, MaterialAsset.Kind.AUDIO,
                "speaking/1000/part2/audio/question_2.mp3", "question-audio.mp3", "audio/mpeg", 3L);
        AtomicLong saveInvocations = new AtomicLong();

        when(materialRepository.save(any(Material.class))).thenAnswer(invocation ->
                saveInvocations.incrementAndGet() == 1 ? persistedScaffold : finalAggregate
        );

        Long materialId = service.uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand.builder()
                .materialTitle("Draft section")
                .partTitle("Part 1")
                .partImage(UploadedFileCommand.builder()
                        .originalFilename("cover.png")
                        .contentType("image/png")
                        .size(3L)
                        .bytes(new byte[]{1, 2, 3})
                        .build())
                .questions(List.of(question("Part 1 question 1")))
                .part2Title("Part 2")
                .part2Questions(List.of(
                        question("Part 2 question 1"),
                        question("Part 2 question 2")
                ))
                .build());

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(2)).save(materialCaptor.capture());
        Material transientAggregate = materialCaptor.getAllValues().getFirst();
        Material assetAugmentedAggregate = materialCaptor.getAllValues().get(1);
        MaterialNode root = transientAggregate.getRoot();
        MaterialNode part1 = root.childAt(0);
        MaterialNode part2 = root.childAt(1);

        assertAll(
                () -> assertThat(materialId).isEqualTo(1000L),
                () -> assertThat(transientAggregate.getId()).isNull(),
                () -> assertThat(transientAggregate.getTitle()).isEqualTo("Draft section"),
                () -> assertThat(transientAggregate.getVersion()).isEqualTo(0L),
                () -> assertThat(root).isNotNull(),
                () -> assertThat(root.getId()).isNull(),
                () -> assertThat(root.getTitle()).isEqualTo("Draft section"),
                () -> assertThat(part1.getId()).isNull(),
                () -> assertThat(part1.getTitle()).isEqualTo("Part 1"),
                () -> assertThat(part2.getId()).isNull(),
                () -> assertThat(part2.getTitle()).isEqualTo("Part 2"),
                () -> assertThat(part1.getChildren()).hasSize(7),
                () -> assertThat(part2.getChildren()).hasSize(4),
                () -> assertThat(part1.getAssets()).isEmpty(),
                () -> assertThat(part1.childAt(0).getAssets()).isEmpty(),
                () -> assertThat(part2.childAt(0).getAssets()).isEmpty(),
                () -> assertThat(part2.childAt(1).getAssets()).isEmpty(),
                () -> assertThat(part1.getChildren()).extracting(MaterialNode::getTranscriptText).containsExactly(
                        "Part 1 question 1", null, null, null, null, null, null),
                () -> assertThat(part2.getChildren()).extracting(MaterialNode::getTranscriptText).containsExactly(
                        "Part 2 question 1", "Part 2 question 2", null, null),
                () -> assertThat(assetAugmentedAggregate.getId()).isEqualTo(1000L),
                () -> assertThat(assetAugmentedAggregate.getVersion()).isEqualTo(4L),
                () -> assertThat(assetAugmentedAggregate.getRoot().getVersion()).isEqualTo(0L),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(0).getVersion()).isEqualTo(0L),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(1).getVersion()).isEqualTo(0L),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9101L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.IMAGE);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/1000/part1/image/image.png");
                    assertThat(asset.getOriginalFilename()).isEqualTo("cover.png");
                    assertThat(asset.getMimeType()).isEqualTo("image/png");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(3L);
                }),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(0).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9110L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/1000/part1/audio/question_1.mp3");
                    assertThat(asset.getOriginalFilename()).isEqualTo("question-audio.mp3");
                    assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(3L);
                }),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(1).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9120L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/1000/part2/audio/question_1.mp3");
                    assertThat(asset.getOriginalFilename()).isEqualTo("question-audio.mp3");
                    assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(3L);
                }),
                () -> assertThat(assetAugmentedAggregate.getRoot().childAt(1).childAt(1).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9121L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/1000/part2/audio/question_2.mp3");
                    assertThat(asset.getOriginalFilename()).isEqualTo("question-audio.mp3");
                    assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(3L);
                })
        );

        var storageKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(4)).uploadObject(eq("toefl"), storageKeyCaptor.capture(), any());
        assertThat(storageKeyCaptor.getAllValues()).containsExactly(
                "speaking/1000/part1/image/image.png",
                "speaking/1000/part1/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_2.mp3"
        );

    }

    @Test
    void uploadSpeakingSection_withoutFiles_buildsCompleteTransientScaffoldBeforeSingleAggregateSave() {
        Material persistedAggregate = persistedSpeakingScaffoldMaterial(
                9000L,
                "Untitled Draft",
                "Draft description"
        );

        when(materialRepository.save(any(Material.class))).thenReturn(persistedAggregate);

        Long materialId = service.uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand.builder()
                .materialTitle("   ")
                .materialDescription("Draft description")
                .partTitle("Part 1")
                .questions(List.of(
                        SpeakingQuestionUploadCommand.builder()
                                .transcriptText("Part 1 question 1")
                                .config(Map.of("prepTimeSeconds", 15))
                                .build()
                ))
                .part2Title("Part 2")
                .part2Questions(List.of(
                        SpeakingQuestionUploadCommand.builder()
                                .transcriptText("Part 2 question 1")
                                .build(),
                        SpeakingQuestionUploadCommand.builder()
                                .transcriptText("Part 2 question 2")
                                .config(Map.of("prepTimeSeconds", 30))
                                .build()
                ))
                .build());

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());
        Material transientAggregate = materialCaptor.getValue();
        MaterialNode root = transientAggregate.getRoot();

        assertAll(
                () -> assertThat(materialId).isEqualTo(persistedAggregate.getId()),
                () -> assertThat(materialCaptor.getAllValues()).hasSize(1),
                () -> assertThat(transientAggregate.getId()).isNull(),
                () -> assertThat(transientAggregate.getTitle()).isEqualTo("Untitled Draft"),
                () -> assertThat(transientAggregate.getDescription()).isEqualTo("Draft description"),
                () -> assertThat(transientAggregate.getVersion()).isEqualTo(0L),
                () -> {
                    MaterialNode verifiedRoot = Objects.requireNonNull(root);

                    List<MaterialNode> rootChildren = verifiedRoot.getChildren().stream()
                            .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                            .toList();
                    MaterialNode part1 = rootChildren.get(0);
                    MaterialNode part2 = rootChildren.get(1);
                    List<MaterialNode> part1Questions = part1.getChildren().stream()
                            .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                            .toList();
                    List<MaterialNode> part2Questions = part2.getChildren().stream()
                            .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                            .toList();

                    assertAll(
                            () -> assertThat(verifiedRoot.getId()).isNull(),
                            () -> assertThat(verifiedRoot.getKind()).isEqualTo(MaterialNodeKind.SECTION),
                            () -> assertThat(verifiedRoot.getTitle()).isEqualTo("Untitled Draft"),
                            () -> assertThat(verifiedRoot.getDisplayOrder()).isEqualTo(0),
                            () -> assertThat(verifiedRoot.getVersion()).isEqualTo(0L),
                            () -> assertThat(rootChildren).hasSize(2),
                            () -> assertThat(part1.getId()).isNull(),
                            () -> assertThat(part1.getTitle()).isEqualTo("Part 1"),
                            () -> assertThat(part1.getDisplayOrder()).isEqualTo(0),
                            () -> assertThat(part1.getVersion()).isEqualTo(0L),
                            () -> assertThat(part2.getId()).isNull(),
                            () -> assertThat(part2.getTitle()).isEqualTo("Part 2"),
                            () -> assertThat(part2.getDisplayOrder()).isEqualTo(1),
                            () -> assertThat(part2.getVersion()).isEqualTo(0L),
                            () -> assertThat(part1Questions).hasSize(7),
                            () -> assertThat(part2Questions).hasSize(4),
                            () -> assertThat(part1Questions).extracting(MaterialNode::getId).containsOnlyNulls(),
                            () -> assertThat(part2Questions).extracting(MaterialNode::getId).containsOnlyNulls(),
                            () -> assertThat(part1Questions).extracting(MaterialNode::getDisplayOrder).containsExactly(0, 1, 2, 3, 4, 5, 6),
                            () -> assertThat(part2Questions).extracting(MaterialNode::getDisplayOrder).containsExactly(0, 1, 2, 3),
                            () -> assertThat(part1Questions).extracting(MaterialNode::getTitle).containsExactly(
                                    "Question 1", "Question 2", "Question 3", "Question 4", "Question 5", "Question 6", "Question 7"),
                            () -> assertThat(part2Questions).extracting(MaterialNode::getTitle).containsExactly(
                                    "Question 1", "Question 2", "Question 3", "Question 4"),
                            () -> assertThat(part1Questions).extracting(MaterialNode::getTranscriptText).containsExactly(
                                    "Part 1 question 1", null, null, null, null, null, null),
                            () -> assertThat(part2Questions).extracting(MaterialNode::getTranscriptText).containsExactly(
                                    "Part 2 question 1", "Part 2 question 2", null, null),
                            () -> assertThat(part1Questions.getFirst().getConfig()).containsEntry("prepTimeSeconds", 15),
                            () -> assertThat(part1Questions.get(1).getConfig()).isEmpty(),
                            () -> assertThat(part2Questions.get(1).getConfig()).containsEntry("prepTimeSeconds", 30),
                            () -> assertThat(part2Questions.get(2).getConfig()).isEmpty()
                    );
                },
                () -> verify(storageRepositoryPort, never()).uploadObject(eq("toefl"), any(String.class), any())
        );
    }

    @Test
    void uploadSpeakingSection_withFiles_attachesInitialAssetsToPersistedAggregateBeforeSingleFinalAggregateSave() {
        Material persistedScaffold = persistedSpeakingScaffoldMaterial(
                9100L,
                "Draft section",
                "Draft description"
        );
        Material finalAggregate = persistedSpeakingScaffoldMaterialWithInitialAssets(
                9100L,
                "Draft section",
                "Draft description"
        );
        AtomicLong saveInvocations = new AtomicLong();

        when(materialRepository.save(any(Material.class))).thenAnswer(invocation ->
                saveInvocations.incrementAndGet() == 1 ? persistedScaffold : finalAggregate
        );

        Long materialId = service.uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand.builder()
                .materialTitle("Draft section")
                .materialDescription("Draft description")
                .partTitle("Part 1")
                .partImage(UploadedFileCommand.builder()
                        .originalFilename("cover.png")
                        .contentType("image/png")
                        .size(3L)
                        .bytes(new byte[]{1, 2, 3})
                        .build())
                .questions(List.of(SpeakingQuestionUploadCommand.builder()
                        .transcriptText("Part 1 question 1")
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("part1-question1.mp3")
                                .contentType("audio/mpeg")
                                .size(11L)
                                .bytes(new byte[]{4, 5, 6})
                                .build())
                        .build()))
                .part2Title("Part 2")
                .part2Questions(List.of(SpeakingQuestionUploadCommand.builder()
                        .transcriptText("Part 2 question 1")
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("part2-question1.mp3")
                                .contentType("audio/mpeg")
                                .size(12L)
                                .bytes(new byte[]{7, 8, 9})
                                .build())
                        .build()))
                .build());

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, atLeastOnce()).save(materialCaptor.capture());
        Material firstSavedAggregate = materialCaptor.getAllValues().getFirst();

        assertAll(
                () -> assertThat(materialId).isEqualTo(finalAggregate.getId()),
                () -> assertThat(materialCaptor.getAllValues()).hasSize(2),
                () -> assertThat(firstSavedAggregate.getId()).isNull(),
                () -> assertThat(firstSavedAggregate.getVersion()).isEqualTo(0L),
                () -> assertThat(firstSavedAggregate.getRoot()).isNotNull(),
                () -> assertThat(firstSavedAggregate.getRoot().childAt(0).getAssets()).isEmpty(),
                () -> assertThat(firstSavedAggregate.getRoot().childAt(0).childAt(0).getAssets()).isEmpty(),
                () -> assertThat(firstSavedAggregate.getRoot().childAt(1).childAt(0).getAssets()).isEmpty(),
                () -> assertThat(persistedScaffold.getVersion()).isEqualTo(3L),
                () -> assertThat(persistedScaffold.getRoot().getVersion()).isEqualTo(0L),
                () -> assertThat(persistedScaffold.getRoot().childAt(0).getVersion()).isEqualTo(0L),
                () -> assertThat(persistedScaffold.getRoot().childAt(0).childAt(0).getVersion()).isEqualTo(0L),
                () -> assertThat(persistedScaffold.getRoot().childAt(1).childAt(0).getVersion()).isEqualTo(0L),
                () -> assertThat(persistedScaffold.getRoot().childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.IMAGE);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9101L);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part1/image/image.png");
                    assertThat(asset.getOriginalFilename()).isEqualTo("cover.png");
                    assertThat(asset.getMimeType()).isEqualTo("image/png");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(3L);
                }),
                () -> assertThat(persistedScaffold.getRoot().childAt(0).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9110L);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part1/audio/question_1.mp3");
                    assertThat(asset.getOriginalFilename()).isEqualTo("part1-question1.mp3");
                    assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(11L);
                }),
                () -> assertThat(persistedScaffold.getRoot().childAt(1).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                    assertThat(asset.getId()).isNull();
                    assertThat(asset.getVersion()).isEqualTo(0L);
                    assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(asset.getMaterialNodeId()).isEqualTo(9120L);
                    assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part2/audio/question_1.mp3");
                    assertThat(asset.getOriginalFilename()).isEqualTo("part2-question1.mp3");
                    assertThat(asset.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(asset.getFileSizeBytes()).isEqualTo(12L);
                }),
                () -> {
                    if (materialCaptor.getAllValues().size() < 2) {
                        return;
                    }

                    Material secondSavedAggregate = materialCaptor.getAllValues().get(1);
                    assertThat(secondSavedAggregate.getId()).isEqualTo(9100L);
                    assertThat(secondSavedAggregate.getVersion()).isEqualTo(3L);
                    assertThat(secondSavedAggregate.getRoot().childAt(0).getAssets()).singleElement().satisfies(asset -> {
                        assertThat(asset.getId()).isNull();
                        assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.IMAGE);
                        assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part1/image/image.png");
                    });
                    assertThat(secondSavedAggregate.getRoot().childAt(0).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                        assertThat(asset.getId()).isNull();
                        assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                        assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part1/audio/question_1.mp3");
                    });
                    assertThat(secondSavedAggregate.getRoot().childAt(1).childAt(0).getAssets()).singleElement().satisfies(asset -> {
                        assertThat(asset.getId()).isNull();
                        assertThat(asset.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                        assertThat(asset.getStorageKey()).isEqualTo("speaking/9100/part2/audio/question_1.mp3");
                    });
                }
        );

        var storageKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(3)).uploadObject(eq("toefl"), storageKeyCaptor.capture(), any());
        assertThat(storageKeyCaptor.getAllValues()).containsExactly(
                "speaking/9100/part1/image/image.png",
                "speaking/9100/part1/audio/question_1.mp3",
                "speaking/9100/part2/audio/question_1.mp3"
        );
    }

    private static SpeakingQuestionUploadCommand question(String transcriptText) {
        return SpeakingQuestionUploadCommand.builder()
                .transcriptText(transcriptText)
                .audio(UploadedFileCommand.builder()
                        .originalFilename("question-audio.mp3")
                        .contentType("audio/mpeg")
                        .size(3L)
                        .bytes(new byte[]{1, 2, 3})
                        .build())
                .build();
    }

    @Test
    void deleteSpeakingSection_delegatesUniqueStorageCleanupRequestsAfterDeleteAndOutboxAppend() {
        Long materialId = 77L;
        Long rootNodeId = 100L;
        Long part1NodeId = 101L;
        Long part1QuestionNodeId = 102L;
        Long part2NodeId = 103L;
        Long part2QuestionNodeId = 104L;

        Material material = Material.builder().id(materialId).version(5L).build();
        MaterialNode root = MaterialNode.builder()
                .id(rootNodeId)
                .materialId(materialId)
                .kind(MaterialNodeKind.SECTION)
                .title("Section")
                .version(2L)
                .build();
        MaterialNode part1 = MaterialNode.builder()
                .id(part1NodeId)
                .materialId(materialId)
                .parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART)
                .displayOrder(0)
                .title("Part 1")
                .version(0L)
                .build();
        MaterialNode part1Question = MaterialNode.builder()
                .id(part1QuestionNodeId)
                .materialId(materialId)
                .parentNodeId(part1NodeId)
                .kind(MaterialNodeKind.ITEM)
                .displayOrder(0)
                .title("Question 1")
                .version(0L)
                .build();
        MaterialNode part2 = MaterialNode.builder()
                .id(part2NodeId)
                .materialId(materialId)
                .parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART)
                .displayOrder(1)
                .title("Part 2")
                .version(0L)
                .build();
        MaterialNode part2Question = MaterialNode.builder()
                .id(part2QuestionNodeId)
                .materialId(materialId)
                .parentNodeId(part2NodeId)
                .kind(MaterialNodeKind.ITEM)
                .displayOrder(0)
                .title("Question 1")
                .version(0L)
                .build();

        root.addAsset(MaterialAsset.builder()
                .id(7000L)
                .materialNodeId(rootNodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/shared/duplicate.png")
                .build());
        part1.addAsset(MaterialAsset.builder()
                .id(7001L)
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/shared/duplicate.png")
                .build());
        part1Question.addAsset(MaterialAsset.builder()
                .id(7002L)
                .materialNodeId(part1QuestionNodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/77/part1/audio/question_1.mp3")
                .build());
        part2Question.addAsset(MaterialAsset.builder()
                .id(7003L)
                .materialNodeId(part2QuestionNodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/77/part2/audio/question_1.mp3")
                .build());
        part2Question.addAsset(MaterialAsset.builder()
                .id(7004L)
                .materialNodeId(part2QuestionNodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey(null)
                .build());

        part1.addChild(part1Question);
        part2.addChild(part2Question);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.deleteSpeakingSection(materialId);

        var eventIdCaptor = forClass(java.util.UUID.class);
        var aggregateTypeCaptor = forClass(String.class);
        var aggregateIdCaptor = forClass(String.class);
        var eventTypeCaptor = forClass(String.class);
        var payloadCaptor = forClass(MaterialDeletedEvent.class);
        var inOrder = inOrder(materialRepository, outboxPort, storageCleanupPort);

        assertAll(
                () -> inOrder.verify(materialRepository, times(1)).delete(materialId),
                () -> inOrder.verify(outboxPort, times(1)).append(
                        eventIdCaptor.capture(),
                        aggregateTypeCaptor.capture(),
                        aggregateIdCaptor.capture(),
                        eventTypeCaptor.capture(),
                        payloadCaptor.capture()),
                () -> inOrder.verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/shared/duplicate.png"),
                () -> inOrder.verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/77/part1/audio/question_1.mp3"),
                () -> inOrder.verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/77/part2/audio/question_1.mp3"),
                () -> verify(storageRepositoryPort, never()).deleteObject(eq("toefl"), any(String.class)),
                () -> verify(storageRepositoryPort, never()).deleteObject(eq("toefl"), isNull())
        );

        assertThat(eventIdCaptor.getValue()).isNotNull();
        assertThat(aggregateTypeCaptor.getValue()).isEqualTo("Material");
        assertThat(aggregateIdCaptor.getValue()).isEqualTo(materialId.toString());
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MATERIAL_DELETED);
        assertThat(payloadCaptor.getValue().getMaterialId()).isEqualTo(materialId);
        assertThat(payloadCaptor.getValue().getRootNodeId()).isEqualTo(rootNodeId);
        assertThat(payloadCaptor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void updateSpeakingSection_replacesAssetAndSchedulesObsoleteStorageKeyDeletionAfterCommit() {
        Long materialId = 88L;
        Long rootNodeId = 200L;
        Long part1NodeId = 201L;

        Material material = Material.builder().id(materialId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        MaterialAsset imageAsset = MaterialAsset.builder()
                .id(5001L)
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/88/part1/image/old-image.png")
                .version(3L)
                .build();
        part1.addAsset(imageAsset);

        UploadedFileCommand newImage = UploadedFileCommand.builder()
                .originalFilename("new-image.png")
                .contentType("image/png")
                .size(3L)
                .bytes(new byte[]{1, 2, 3})
                .build();

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .partImage(newImage)
                .build();

        service.updateSpeakingSection(command);

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();
        assertImmutableImageReplacementKey(replacementKey, materialId, 1, "speaking/88/part1/image/old-image.png");
        assertThat(part1.getAssets()).singleElement().satisfies(image -> assertThat(image.getStorageKey()).isEqualTo(replacementKey));
        verify(storageRepositoryPort, never()).deleteObject("toefl", "speaking/88/part1/image/old-image.png");
        verify(storageRepositoryPort, never()).deleteObject("toefl", replacementKey);
        verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/88/part1/image/old-image.png");
        verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey);
        verify(materialRepository, times(1)).save(material);
    }

    @Test
    void updateSpeakingSection_existingQuestionAudioReplacement_updatesAttachedAggregateAndSavesItOnce() {
        Long materialId = 91L;
        Long rootNodeId = 410L;
        Long part1NodeId = 411L;
        Long questionNodeId = 412L;
        Long assetId = 6100L;
        String existingKey = "speaking/91/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        MaterialAsset attachedAudio = audioAsset(assetId, questionNodeId, existingKey, "old-question.mp3", 1000L, 5L);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("replacement.mp3")
                                .contentType("audio/mpeg")
                                .size(12345L)
                                .bytes(new byte[]{1, 2, 3})
                                .build())
                        .build()))
                .build());

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();

        assertAll(
                () -> {
                    MaterialAsset aggregateAudio = question.getAssets().getFirst();
                    assertThat(aggregateAudio.getId()).isEqualTo(assetId);
                    assertImmutableAudioReplacementKey(replacementKey, materialId, 1, 1, existingKey);
                    assertThat(aggregateAudio.getStorageKey()).isEqualTo(replacementKey);
                    assertThat(aggregateAudio.getOriginalFilename()).isEqualTo("replacement.mp3");
                    assertThat(aggregateAudio.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(aggregateAudio.getFileSizeBytes()).isEqualTo(12345L);
                    assertThat(aggregateAudio.getVersion()).isEqualTo(6L);
                    assertThat(material.getVersion()).isEqualTo(2L);
                },
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    MaterialAsset savedAudio = saved.getRoot().childAt(0).childAt(0).getAssets().getFirst();
                    assertThat(saved).isSameAs(material);
                    assertThat(savedAudio).isSameAs(attachedAudio);
                    assertThat(savedAudio.getId()).isEqualTo(assetId);
                    assertThat(savedAudio.getStorageKey()).isEqualTo(replacementKey);
                    assertThat(savedAudio.getOriginalFilename()).isEqualTo("replacement.mp3");
                    assertThat(savedAudio.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(savedAudio.getFileSizeBytes()).isEqualTo(12345L);
                    assertThat(savedAudio.getVersion()).isEqualTo(6L);
                    assertThat(saved.getVersion()).isEqualTo(2L);
                },
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", existingKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", replacementKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_questionContentAndExistingAudioReplacement_savesSingleAggregateWithBothChanges() {
        Long materialId = 92L;
        Long rootNodeId = 420L;
        Long part1NodeId = 421L;
        Long questionNodeId = 422L;
        Long assetId = 6200L;
        String existingKey = "speaking/92/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        question.updateConfig(Map.of("responseTimeSeconds", 30));
        MaterialAsset attachedAudio = audioAsset(assetId, questionNodeId, existingKey, "old-question.mp3", 1000L, 5L);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        Map<String, Object> updatedConfig = Map.of("responseTimeSeconds", 45);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .transcriptText("Updated transcript")
                        .config(updatedConfig)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("replacement.mp3")
                                .contentType("audio/mpeg")
                                .size(12345L)
                                .bytes(new byte[]{4, 5, 6})
                                .build())
                        .build()))
                .build());

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();

        assertAll(
                () -> {
                    MaterialAsset aggregateAudio = question.getAssets().getFirst();
                    assertThat(question.getTranscriptText()).isEqualTo("Updated transcript");
                    assertThat(question.getConfig()).isEqualTo(updatedConfig);
                    assertThat(aggregateAudio.getId()).isEqualTo(assetId);
                    assertImmutableAudioReplacementKey(replacementKey, materialId, 1, 1, existingKey);
                    assertThat(aggregateAudio.getStorageKey()).isEqualTo(replacementKey);
                    assertThat(aggregateAudio.getOriginalFilename()).isEqualTo("replacement.mp3");
                    assertThat(aggregateAudio.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(aggregateAudio.getFileSizeBytes()).isEqualTo(12345L);
                    assertThat(aggregateAudio.getVersion()).isEqualTo(6L);
                    assertThat(material.getVersion()).isEqualTo(4L);
                },
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    MaterialNode savedQuestion = saved.getRoot().childAt(0).childAt(0);
                    MaterialAsset savedAudio = savedQuestion.getAssets().getFirst();
                    assertThat(savedQuestion.getTranscriptText()).isEqualTo("Updated transcript");
                    assertThat(savedQuestion.getConfig()).isEqualTo(updatedConfig);
                    assertThat(savedAudio).isSameAs(attachedAudio);
                    assertThat(savedAudio.getId()).isEqualTo(assetId);
                    assertThat(savedAudio.getStorageKey()).isEqualTo(replacementKey);
                    assertThat(savedAudio.getOriginalFilename()).isEqualTo("replacement.mp3");
                    assertThat(savedAudio.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(savedAudio.getFileSizeBytes()).isEqualTo(12345L);
                    assertThat(savedAudio.getVersion()).isEqualTo(6L);
                    assertThat(saved.getVersion()).isEqualTo(4L);
                },
                () -> verify(materialRepository, times(1)).save(any(Material.class)),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", existingKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", replacementKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_newQuestionAudio_attachesNewAggregateAssetAndSavesOnce() {
        Long materialId = 93L;
        Long rootNodeId = 430L;
        Long part1NodeId = 431L;
        Long questionNodeId = 432L;
        String newKey = "speaking/93/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("question.mp3")
                                .contentType("audio/mpeg")
                                .size(321L)
                                .bytes(new byte[]{1, 2, 3})
                                .build())
                        .build()))
                .build());

        assertAll(
                () -> assertThat(question.getAssets()).singleElement().satisfies(created -> {
                    assertThat(created.getId()).isNull();
                    assertThat(created.getMaterialNodeId()).isEqualTo(questionNodeId);
                    assertThat(created.getKind()).isEqualTo(MaterialAsset.Kind.AUDIO);
                    assertThat(created.getStorageKey()).isEqualTo(newKey);
                    assertThat(created.getOriginalFilename()).isEqualTo("question.mp3");
                    assertThat(created.getMimeType()).isEqualTo("audio/mpeg");
                    assertThat(created.getFileSizeBytes()).isEqualTo(321L);
                    assertThat(created.getVersion()).isEqualTo(0L);
                }),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved).isSameAs(material);
                    assertThat(saved.getVersion()).isEqualTo(2L);
                    assertThat(saved.getRoot().childAt(0).childAt(0).getVersion()).isNull();
                    assertThat(saved.getRoot().childAt(0).childAt(0).getAssets()).singleElement().satisfies(created -> {
                        assertThat(created.getId()).isNull();
                        assertThat(created.getStorageKey()).isEqualTo(newKey);
                        assertThat(created.getVersion()).isEqualTo(0L);
                    });
                },
                () -> verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), eq(newKey), any())
        );
    }

    @Test
    void updateSpeakingSection_existingPartImageReplacement_updatesAttachedAggregateAndSavesOnce() {
        Long materialId = 94L;
        Long rootNodeId = 440L;
        Long part1NodeId = 441L;
        Long imageAssetId = 6400L;
        String imageKey = "speaking/94/part1/image/image.png";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialAsset attachedImage = imageAsset(imageAssetId, part1NodeId, imageKey, "old-image.png", 10L, 5L);
        part1.addAsset(attachedImage);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .partImage(UploadedFileCommand.builder()
                        .originalFilename("replacement.png")
                        .contentType("image/png")
                        .size(222L)
                        .bytes(new byte[]{4, 5, 6})
                        .build())
                .build());

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();

        assertAll(
                () -> assertThat(part1.getAssets()).singleElement().satisfies(image -> {
                    assertThat(image.getId()).isEqualTo(imageAssetId);
                    assertImmutableImageReplacementKey(replacementKey, materialId, 1, imageKey);
                    assertThat(image.getStorageKey()).isEqualTo(replacementKey);
                    assertThat(image.getOriginalFilename()).isEqualTo("replacement.png");
                    assertThat(image.getMimeType()).isEqualTo("image/png");
                    assertThat(image.getFileSizeBytes()).isEqualTo(222L);
                    assertThat(image.getVersion()).isEqualTo(6L);
                }),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved.getVersion()).isEqualTo(2L);
                    assertThat(saved.getRoot().childAt(0).getVersion()).isEqualTo(1L);
                    assertThat(saved.getRoot().childAt(0).getAssets()).singleElement().satisfies(image -> {
                        assertThat(image.getId()).isEqualTo(imageAssetId);
                        assertThat(image.getStorageKey()).isEqualTo(replacementKey);
                        assertThat(image.getOriginalFilename()).isEqualTo("replacement.png");
                        assertThat(image.getVersion()).isEqualTo(6L);
                    });
                },
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", imageKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", replacementKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", imageKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_existingQuestionAudioReplacement_whenSaveFails_compensatesOnlyNewlyUploadedKey() {
        Long materialId = 109L;
        Long rootNodeId = 590L;
        Long part1NodeId = 591L;
        Long questionNodeId = 592L;
        Long assetId = 6900L;
        String existingKey = "speaking/109/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        MaterialAsset attachedAudio = audioAsset(assetId, questionNodeId, existingKey, "old-question.mp3", 1000L, 5L);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenThrow(new IllegalStateException("db save failed"));

        assertThatThrownBy(() -> service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("replacement.mp3")
                                .contentType("audio/mpeg")
                                .size(12345L)
                                .bytes(new byte[]{1, 2, 3})
                                .build())
                        .build()))
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db save failed");

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();

        assertAll(
                () -> assertImmutableAudioReplacementKey(replacementKey, materialId, 1, 1, existingKey),
                () -> assertThat(attachedAudio.getStorageKey()).isEqualTo(replacementKey),
                () -> verify(storageRepositoryPort, times(1)).deleteObject("toefl", replacementKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_whenCompensationDeletionFails_preservesOriginalSaveFailure() {
        Long materialId = 110L;
        Long rootNodeId = 600L;
        Long part1NodeId = 601L;
        Long questionNodeId = 602L;
        Long assetId = 7000L;
        String existingKey = "speaking/110/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        MaterialAsset attachedAudio = audioAsset(assetId, questionNodeId, existingKey, "old-question.mp3", 1000L, 5L);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenThrow(new IllegalStateException("db save failed"));

        String replacementKey = "speaking/110/part1/audio/question_1/550e8400-e29b-41d4-a716-446655440000.mp3";
        RuntimeException cleanupFailure = new RuntimeException("cleanup delete failed");
        doThrow(cleanupFailure).when(storageRepositoryPort).deleteObject("toefl", replacementKey);

        assertThatThrownBy(() -> service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("replacement.mp3")
                                .contentType("audio/mpeg")
                                .size(12345L)
                                .bytes(new byte[]{1, 2, 3})
                                .build())
                        .build()))
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db save failed")
                .isNotSameAs(cleanupFailure);

        assertAll(
                () -> verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), eq(replacementKey), any()),
                () -> verify(storageRepositoryPort, times(1)).deleteObject("toefl", replacementKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", existingKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_newPartImage_attachesNewAggregateAssetAndSavesOnce() {
        Long materialId = 95L;
        Long rootNodeId = 450L;
        Long part1NodeId = 451L;
        String imageKey = "speaking/95/part1/image/image.png";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .partImage(UploadedFileCommand.builder()
                        .originalFilename("cover.png")
                        .contentType("image/png")
                        .size(444L)
                        .bytes(new byte[]{9, 8, 7})
                        .build())
                .build());

        assertAll(
                () -> assertThat(part1.getAssets()).singleElement().satisfies(image -> {
                    assertThat(image.getId()).isNull();
                    assertThat(image.getMaterialNodeId()).isEqualTo(part1NodeId);
                    assertThat(image.getKind()).isEqualTo(MaterialAsset.Kind.IMAGE);
                    assertThat(image.getStorageKey()).isEqualTo(imageKey);
                    assertThat(image.getOriginalFilename()).isEqualTo("cover.png");
                    assertThat(image.getMimeType()).isEqualTo("image/png");
                    assertThat(image.getFileSizeBytes()).isEqualTo(444L);
                    assertThat(image.getVersion()).isEqualTo(0L);
                }),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved.getVersion()).isEqualTo(2L);
                    assertThat(saved.getRoot().childAt(0).getVersion()).isEqualTo(1L);
                },
                () -> verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), eq(imageKey), any())
        );
    }

    @Test
    void updateSpeakingSection_removePartImage_schedulesDuplicateObsoleteStorageKeyOnceAfterCommit() {
        Long materialId = 89L;
        Long rootNodeId = 300L;
        Long part1NodeId = 301L;

        Material material = Material.builder().id(materialId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        MaterialAsset imageAsset = MaterialAsset.builder()
                .id(5000L)
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/89/part1/image/old-image.png")
                .build();
        MaterialAsset duplicateImageAsset = MaterialAsset.builder()
                .id(5002L)
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/89/part1/image/old-image.png")
                .build();
        part1.addAsset(imageAsset);
        part1.addAsset(duplicateImageAsset);

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .removePartImage(true)
                .build();

        service.updateSpeakingSection(command);

        assertThat(part1.getAssets()).isEmpty();
        verify(materialRepository, times(1)).save(material);
        verify(storageRepositoryPort, never()).deleteObject("toefl", "speaking/89/part1/image/old-image.png");
        verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/89/part1/image/old-image.png");
    }

    @Test
    void updateSpeakingSection_existingPartImageRemoval_removesFromAggregateAndSavesOnce() {
        Long materialId = 96L;
        Long rootNodeId = 460L;
        Long part1NodeId = 461L;
        Long assetId = 6500L;
        String oldKey = "speaking/96/part1/image/image.png";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialAsset attachedImage = imageAsset(assetId, part1NodeId, oldKey, "cover.png", 100L, 5L);
        part1.addAsset(attachedImage);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .removePartImage(true)
                .build());

        assertAll(
                () -> assertThat(part1.getAssets()).isEmpty(),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved.getVersion()).isEqualTo(2L);
                    assertThat(saved.getRoot().childAt(0).getVersion()).isEqualTo(1L);
                    assertThat(saved.getRoot().childAt(0).getAssets()).isEmpty();
                },
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", oldKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", oldKey)
        );
    }

    @Test
    void updateSpeakingSection_removeQuestionAudio_deletesAssetEntryAndStorageKey() {
        Long materialId = 90L;
        Long rootNodeId = 400L;
        Long part1NodeId = 401L;
        Long questionNodeId = 402L;

        Material material = Material.builder().id(materialId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        MaterialNode q0 = MaterialNode.builder().id(questionNodeId).materialId(materialId).parentNodeId(part1NodeId).displayOrder(0).version(1L).build();
        part1.addChild(q0);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        MaterialAsset audioAsset = MaterialAsset.builder()
                .id(6000L)
                .materialNodeId(questionNodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/90/part1/audio/old-question.mp3")
                .build();
        q0.addAsset(audioAsset);

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(
                        SpeakingQuestionPartialUpdateCommand.builder().index(0).removeAudio(true).build()
                ))
                .build();

        service.updateSpeakingSection(command);

        assertThat(q0.getAssets()).isEmpty();
        verify(storageRepositoryPort, never()).deleteObject("toefl", "speaking/90/part1/audio/old-question.mp3");
        verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", "speaking/90/part1/audio/old-question.mp3");
        assertThat(material.getVersion()).isEqualTo(2L);
        verify(materialRepository, times(1)).save(material);
    }

    @Test
    void updateSpeakingSection_existingQuestionAudioRemoval_removesFromAggregateAndSavesOnce() {
        Long materialId = 97L;
        Long rootNodeId = 470L;
        Long part1NodeId = 471L;
        Long questionNodeId = 472L;
        Long assetId = 6600L;
        String oldKey = "speaking/97/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        MaterialAsset attachedAudio = audioAsset(assetId, questionNodeId, oldKey, "old-question.mp3", 100L, 5L);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder().index(0).removeAudio(true).build()))
                .build());

        assertAll(
                () -> assertThat(question.getAssets()).isEmpty(),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved.getVersion()).isEqualTo(2L);
                    assertThat(saved.getRoot().childAt(0).childAt(0).getVersion()).isNull();
                    assertThat(saved.getRoot().childAt(0).childAt(0).getAssets()).isEmpty();
                },
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", oldKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", oldKey)
        );
    }

    @Test
    void updateSpeakingSection_whenValidationFailsBeforeCleanupScheduling_doesNotRequestObsoleteDeletion() {
        Long materialId = 108L;
        Long rootNodeId = 580L;
        Long part1NodeId = 581L;

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        part1.addAsset(imageAsset(6800L, part1NodeId, "speaking/108/part1/image/old-image.png", "cover.png", 100L, 5L));
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .partImage(UploadedFileCommand.builder()
                        .originalFilename("replacement.png")
                        .contentType("image/png")
                        .size(321L)
                        .bytes(new byte[]{1, 2, 3})
                        .build())
                .removePartImage(true)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partImage and removePartImage cannot both be set");

        verify(materialRepository, never()).save(any(Material.class));
        verify(storageRepositoryPort, never()).deleteObject(eq("toefl"), any(String.class));
        verifyNoInteractions(storageCleanupPort);
    }

    @Test
    void updateSpeakingSection_absentImageAndAudioRemoval_isIdempotentApplicationNoOp() {
        Long materialId = 98L;
        Long rootNodeId = 480L;
        Long part1NodeId = 481L;
        Long questionNodeId = 482L;

        Material material = Material.builder().id(materialId).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .removePartImage(true)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder().index(0).removeAudio(true).build()))
                .build());

        assertAll(
                () -> assertThat(part1.getAssets()).isEmpty(),
                () -> assertThat(question.getAssets()).isEmpty(),
                () -> assertThat(material.getVersion()).isEqualTo(1L),
                () -> assertThat(material.getUpdatedAt()).isEqualTo(ORIGINAL_TIME),
                () -> verify(materialRepository, never()).save(any(Material.class))
        );
    }

    @Test
    void updateSpeakingSection_mixedTitleContentAudioReplacementAndImageRemoval_savesSingleFinalAggregate() {
        Long materialId = 99L;
        Long rootNodeId = 490L;
        Long part1NodeId = 491L;
        Long part2NodeId = 493L;
        Long questionNodeId = 492L;
        Long imageAssetId = 6700L;
        Long audioAssetId = 6701L;
        String imageKey = "speaking/99/part1/image/image.png";
        String audioKey = "speaking/99/part1/audio/question_1.mp3";

        Material material = Material.builder().id(materialId).title("Old Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Old Material").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Part 1").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(1).title("Part 2").version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, 0, "Original transcript");
        MaterialAsset attachedImage = imageAsset(imageAssetId, part1NodeId, imageKey, "cover.png", 100L, 5L);
        MaterialAsset attachedAudio = audioAsset(audioAssetId, questionNodeId, audioKey, "old-question.mp3", 200L, 5L);
        part1.addAsset(attachedImage);
        question.addAsset(attachedAudio);
        part1.addChild(question);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        Map<String, Object> updatedConfig = Map.of("responseTimeSeconds", 45);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .materialTitle("New Material")
                .removePartImage(true)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(0)
                        .transcriptText("Updated transcript")
                        .config(updatedConfig)
                        .audio(UploadedFileCommand.builder()
                                .originalFilename("replacement.mp3")
                                .contentType("audio/mpeg")
                                .size(333L)
                                .bytes(new byte[]{7, 7, 7})
                                .build())
                        .build()))
                .build());

        var uploadKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(1)).uploadObject(eq("toefl"), uploadKeyCaptor.capture(), any());
        String replacementKey = uploadKeyCaptor.getValue();

        assertAll(
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    MaterialNode savedPart1 = saved.getRoot().childAt(0);
                    MaterialNode savedQuestion = savedPart1.childAt(0);
                    assertThat(saved.getTitle()).isEqualTo("New Material");
                    assertThat(saved.getRoot().getTitle()).isEqualTo("New Material");
                    assertThat(savedPart1.getAssets()).isEmpty();
                    assertThat(savedQuestion.getTranscriptText()).isEqualTo("Updated transcript");
                    assertThat(savedQuestion.getConfig()).isEqualTo(updatedConfig);
                    assertThat(savedQuestion.getAssets()).singleElement().satisfies(audio -> {
                        assertThat(audio.getId()).isEqualTo(audioAssetId);
                        assertImmutableAudioReplacementKey(replacementKey, materialId, 1, 1, audioKey);
                        assertThat(audio.getStorageKey()).isEqualTo(replacementKey);
                        assertThat(audio.getOriginalFilename()).isEqualTo("replacement.mp3");
                    });
                },
                () -> verify(materialRepository, times(1)).save(any(Material.class)),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", imageKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", audioKey),
                () -> verify(storageRepositoryPort, never()).deleteObject("toefl", replacementKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", imageKey),
                () -> verify(storageCleanupPort, times(1)).requestDeletion(materialId, "toefl", audioKey),
                () -> verify(storageCleanupPort, never()).requestDeletion(materialId, "toefl", replacementKey)
        );
    }

    @Test
    void updateSpeakingSection_titleOnlyUpdate_persistsThroughMaterialAggregate() {
        Long materialId = 905L;
        Material material = Material.builder().id(materialId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(950L).materialId(materialId)
                .kind(MaterialNodeKind.SECTION).title("Old Material").build();
        MaterialNode part1 = MaterialNode.builder().id(951L).materialId(materialId).parentNodeId(root.getId())
                .kind(MaterialNodeKind.PART).displayOrder(0).title("Old Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(952L).materialId(materialId).parentNodeId(root.getId())
                .kind(MaterialNodeKind.PART).displayOrder(1).title("Old Part 2").build();
        root.addChild(part2);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .materialTitle("New Material")
                .partTitle("New Part 1")
                .part2Title("New Part 2")
                .build());

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());
        Material saved = materialCaptor.getValue();
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort, times(1)).append(any(), eq("Material"), eq(materialId.toString()),
                eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
        MaterialDetailsUpsertedEvent event = eventCaptor.getValue();

        assertAll(
                () -> {
                    assertThat(saved).isSameAs(material);
                    assertThat(saved.getRoot()).isSameAs(root);
                    assertThat(saved.getRoot().getChildren()).hasSize(2);
                    assertThat(saved.getRoot().childAt(0)).isSameAs(part1);
                    assertThat(saved.getRoot().childAt(1)).isSameAs(part2);
                    assertThat(saved.getTitle()).isEqualTo("New Material");
                    assertThat(root.getTitle()).isEqualTo("New Material");
                    assertThat(part1.getTitle()).isEqualTo("New Part 1");
                    assertThat(part2.getTitle()).isEqualTo("New Part 2");
                    assertThat(saved.getVersion()).isEqualTo(4L);
                    assertThat(event.getMaterialId()).isEqualTo(materialId);
                    assertThat(event.getMaterialTitle()).isEqualTo(saved.getTitle());
                    assertThat(event.getVersion()).isEqualTo(saved.getVersion());
                },
                () -> assertThat(event.getPart1Title()).isEqualTo(root.childAt(0).getTitle()),
                () -> assertThat(event.getPart2Title()).isEqualTo(root.childAt(1).getTitle())
        );
    }

    @Test
    void updateSpeakingSection_titlesChanged_appendsSingleCompactTitlesEventToOutbox() {
        Long materialId = 901L;
        Long rootNodeId = 910L;
        Long part1NodeId = 911L;
        Long part2NodeId = 912L;

        Material material = Material.builder().id(materialId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Old Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).title("Old Part 1").version(1L).build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(1).title("Old Part 2").version(1L).build();
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .materialTitle("New Material")
                .partTitle("New Part 1")
                .part2Title("New Part 2")
                .build();

        service.updateSpeakingSection(command);

        var eventIdCaptor = forClass(java.util.UUID.class);
        var aggregateTypeCaptor = forClass(String.class);
        var aggregateIdCaptor = forClass(String.class);
        var eventTypeCaptor = forClass(String.class);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort, times(1)).append(
                eventIdCaptor.capture(),
                aggregateTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                eventTypeCaptor.capture(),
                eventCaptor.capture());
        assertThat(eventIdCaptor.getValue()).isNotNull();
        assertThat(aggregateTypeCaptor.getValue()).isEqualTo("Material");
        assertThat(aggregateIdCaptor.getValue()).isEqualTo(materialId.toString());
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MATERIAL_DETAILS_UPSERTED);

        MaterialDetailsUpsertedEvent event = eventCaptor.getValue();
        assertThat(event.getMaterialId()).isEqualTo(materialId);
        assertThat(event.getVersion()).isEqualTo(4L);
        assertThat(event.getMaterialTitle()).isEqualTo("New Material");
        assertThat(event.getPart1Title()).isEqualTo("New Part 1");
        assertThat(event.getPart2Title()).isEqualTo("New Part 2");
        assertThat(event.getDescription()).isNull();
        assertThat(event.getUpdatedAt()).isNotNull();

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());
        assertThat(materialCaptor.getValue().getVersion()).isEqualTo(4L);
    }

    @Test
    void updateSpeakingSection_partTitlesChanged_bumpsMaterialVersionAndAppendsVersionedTitlesEventToOutbox() {
        Long materialId = 903L;
        Long rootNodeId = 930L;
        Long part1NodeId = 931L;
        Long part2NodeId = 932L;

        Material material = Material.builder().id(materialId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Old Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).title("Old Part 1").version(1L).build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(1).title("Old Part 2").version(1L).build();
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .partTitle("New Part 1")
                .part2Title("New Part 2")
                .build();

        service.updateSpeakingSection(command);

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());
        assertThat(materialCaptor.getValue().getVersion()).isEqualTo(3L);
        assertThat(part1.getVersion()).isEqualTo(2L);
        assertThat(part2.getVersion()).isEqualTo(2L);
        assertThat(root.getVersion()).isEqualTo(1L);

        var eventIdCaptor = forClass(java.util.UUID.class);
        var aggregateTypeCaptor = forClass(String.class);
        var aggregateIdCaptor = forClass(String.class);
        var eventTypeCaptor = forClass(String.class);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort, times(1)).append(
                eventIdCaptor.capture(),
                aggregateTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                eventTypeCaptor.capture(),
                eventCaptor.capture());
        assertThat(eventIdCaptor.getValue()).isNotNull();
        assertThat(aggregateTypeCaptor.getValue()).isEqualTo("Material");
        assertThat(aggregateIdCaptor.getValue()).isEqualTo(materialId.toString());
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MATERIAL_DETAILS_UPSERTED);

        MaterialDetailsUpsertedEvent event = eventCaptor.getValue();
        assertThat(event.getMaterialId()).isEqualTo(materialId);
        assertThat(event.getVersion()).isEqualTo(3L);
        assertThat(event.getMaterialTitle()).isEqualTo("Old Material");
        assertThat(event.getPart1Title()).isEqualTo("New Part 1");
        assertThat(event.getPart2Title()).isEqualTo("New Part 2");
        assertThat(event.getDescription()).isNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateSpeakingSection_textOnlyQuestionUpdate_persistsThroughMaterialAggregate() {
        Long materialId = 90L;
        Long rootNodeId = 400L;
        Long part1NodeId = 401L;
        Long questionNodeId = 402L;
        int questionIndex = 3;

        Material material = Material.builder().id(materialId).title("Material").version(1L).build();
        MaterialNode root = MaterialNode.builder()
                .id(rootNodeId).materialId(materialId).kind(MaterialNodeKind.SECTION).build();
        MaterialNode part1 = MaterialNode.builder()
                .id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId)
                .kind(MaterialNodeKind.PART).displayOrder(0).build();
        MaterialNode question = questionNode(materialId, questionNodeId, part1NodeId, questionIndex, "Old transcript");
        part1.addChild(question);
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        Map<String, Object> updatedConfig = Map.of("responseTimeSeconds", 45);
        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(SpeakingQuestionPartialUpdateCommand.builder()
                        .index(questionIndex)
                        .transcriptText("New transcript")
                        .config(updatedConfig)
                        .build()))
                .build();

        service.updateSpeakingSection(command);

        assertAll(
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository).save(materialCaptor.capture());
                    Material saved = materialCaptor.getValue();
                    assertThat(saved).isSameAs(material);
                    MaterialNode savedQuestion = saved.getRoot().childAt(0).childAt(questionIndex);
                    assertThat(savedQuestion).isSameAs(question);
                    assertThat(savedQuestion.getTranscriptText()).isEqualTo("New transcript");
                    assertThat(savedQuestion.getConfig()).isEqualTo(updatedConfig);
                    assertThat(saved.getVersion()).isEqualTo(3L);
                    assertThat(saved.getUpdatedAt()).isNotNull();
                    assertThat(savedQuestion.getVersion()).isEqualTo(2L);
                }
        );
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void updateSpeakingSection_multipleQuestionsAndTitles_savesFinalAggregateOnce() {
        Long materialId = 904L;
        Material material = Material.builder().id(materialId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(940L).materialId(materialId).title("Old Material").build();
        MaterialNode part1 = MaterialNode.builder().id(941L).materialId(materialId).parentNodeId(root.getId())
                .displayOrder(0).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(942L).materialId(materialId).parentNodeId(root.getId())
                .displayOrder(1).title("Old Part 2").build();
        MaterialNode first = questionNode(materialId, 943L, part1.getId(), 3, "Old first");
        MaterialNode second = questionNode(materialId, 944L, part1.getId(), 0, "Old second");
        MaterialNode third = questionNode(materialId, 945L, part2.getId(), 2, "Unchanged transcript");
        part1.addChild(first);
        part1.addChild(second);
        part2.addChild(third);
        root.addChild(part2);
        root.addChild(part1);
        material.attachRoot(root);
        Map<String, Object> config = Map.of("responseTimeSeconds", 45);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material saved = invocation.getArgument(0);
            // Check at invocation time so an early save cannot pass through later object mutation.
            assertThat(saved).isSameAs(material);
            assertThat(saved.getTitle()).isEqualTo("New Material");
            assertThat(saved.getVersion()).isEqualTo(6L);
            assertThat(saved.getRoot().getTitle()).isEqualTo("New Material");
            assertThat(saved.getRoot().childAt(0).childAt(3).getTranscriptText()).isEqualTo("New first");
            assertThat(saved.getRoot().childAt(0).childAt(0).getTranscriptText()).isEqualTo("New second");
            assertThat(saved.getRoot().childAt(1).getTitle()).isEqualTo("New Part 2");
            assertThat(saved.getRoot().childAt(1).childAt(2).getConfig()).isEqualTo(config);
            assertThat(saved.getRoot().childAt(1).childAt(2).getTranscriptText()).isEqualTo("Unchanged transcript");
            return saved;
        });

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId).materialTitle("New Material").part2Title("New Part 2")
                .questions(List.of(
                        SpeakingQuestionPartialUpdateCommand.builder().index(3).transcriptText("New first").build(),
                        SpeakingQuestionPartialUpdateCommand.builder().index(0).transcriptText("New second").build()))
                .part2Questions(List.of(SpeakingQuestionPartialUpdateCommand.builder().index(2).config(config).build()))
                .build());

        verify(materialRepository).save(material);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort).append(any(), eq("Material"), eq(materialId.toString()),
                eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getVersion()).isEqualTo(6L);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void updateSpeakingSection_singlePartTitleChange_versionsAggregateOnce(int partIndex) {
        Material material = editableMaterial();
        MaterialNode part = material.getRoot().childAt(partIndex);

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .partTitle(partIndex == 0 ? "  Revised part  " : "Part 1")
                .part2Title(partIndex == 1 ? "  Revised part  " : "Part 2")
                .build());

        assertThat(part.getTitle()).isEqualTo("Revised part");
        assertThat(part.getVersion()).isEqualTo(4L);
        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(material.getRoot().getVersion()).isEqualTo(3L);
        assertThat(material.getRoot().childAt(1 - partIndex).getVersion()).isEqualTo(3L);
        verify(materialRepository).save(material);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort).append(any(), eq("Material"), eq(material.getId().toString()),
                eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getVersion()).isEqualTo(2L);
    }

    @ParameterizedTest
    @CsvSource({"0,true,false", "0,false,true", "0,true,true",
            "1,true,false", "1,false,true", "1,true,true"})
    void updateSpeakingSection_questionOnly_versionsEachChangedFieldAndSavesOnce(
            int partIndex, boolean changeTranscript, boolean changeConfig) {
        Material material = editableMaterial();
        MaterialNode question = material.getRoot().childAt(partIndex).childAt(0);
        var questionUpdate = SpeakingQuestionPartialUpdateCommand.builder()
                .index(0)
                .transcriptText(changeTranscript ? "  Revised transcript  " : null)
                .config(changeConfig ? Map.of("responseTimeSeconds", 60) : null)
                .build();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .questions(partIndex == 0 ? List.of(questionUpdate) : null)
                .part2Questions(partIndex == 1 ? List.of(questionUpdate) : null)
                .build());

        long changes = (changeTranscript ? 1L : 0L) + (changeConfig ? 1L : 0L);
        assertThat(material.getVersion()).isEqualTo(1L + changes);
        assertThat(question.getVersion()).isEqualTo(3L + changes);
        assertThat(material.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(question.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(question.getTranscriptText()).isEqualTo(changeTranscript ? "Revised transcript" : "Original transcript");
        assertThat(question.getConfig()).containsExactlyEntriesOf(
                Map.of("responseTimeSeconds", changeConfig ? 60 : 30));
        assertThat(material.getRoot().getVersion()).isEqualTo(3L);
        assertThat(material.getRoot().childAt(partIndex).getVersion()).isEqualTo(3L);
        verify(materialRepository).save(material);
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void updateSpeakingSection_equalChildValues_doNotVersionSaveOrPublish(String padding) {
        Material material = editableMaterial();
        var questionUpdate = SpeakingQuestionPartialUpdateCommand.builder()
                .index(0)
                .transcriptText(padding + "Original transcript" + padding)
                .config(new HashMap<>(Map.of("responseTimeSeconds", 30)))
                .build();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .partTitle(padding + "Part 1" + padding)
                .part2Title(padding + "Part 2" + padding)
                .questions(List.of(questionUpdate))
                .part2Questions(List.of(questionUpdate))
                .build());

        assertThat(material.getVersion()).isEqualTo(1L);
        assertThat(material.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        for (MaterialNode part : material.getRoot().getChildren()) {
            assertThat(part.getVersion()).isEqualTo(3L);
            assertThat(part.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
            assertThat(part.childAt(0).getVersion()).isEqualTo(3L);
            assertThat(part.childAt(0).getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        }
        verify(materialRepository, never()).save(any());
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void updateSpeakingSection_questionChangeFollowedByNoOp_stillSavesOnce(int partIndex) {
        Material material = editableMaterial();
        var updates = List.of(
                SpeakingQuestionPartialUpdateCommand.builder().index(0).transcriptText("Revised transcript").build(),
                SpeakingQuestionPartialUpdateCommand.builder().index(0).transcriptText("  Revised transcript  ")
                        .config(new HashMap<>(Map.of("responseTimeSeconds", 30))).build());

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .questions(partIndex == 0 ? updates : null)
                .part2Questions(partIndex == 1 ? updates : null)
                .build());

        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getRoot().childAt(partIndex).childAt(0).getVersion()).isEqualTo(4L);
        verify(materialRepository).save(material);
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void updateSpeakingSection_materialTitleOnly_mirrorsRootWithoutDoubleVersioning() {
        Material material = editableMaterial();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .materialTitle("New Material")
                .partTitle("Part 1")
                .part2Title("Part 2")
                .build());

        assertThat(material.getTitle()).isEqualTo("New Material");
        assertThat(material.getRoot().getTitle()).isEqualTo("New Material");
        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getRoot().getVersion()).isEqualTo(4L);
        assertThat(material.getRoot().childAt(0).getVersion()).isEqualTo(3L);
        assertThat(material.getRoot().childAt(1).getVersion()).isEqualTo(3L);
        verify(materialRepository).save(material);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort).append(any(), eq("Material"), eq(material.getId().toString()),
                eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getVersion()).isEqualTo(2L);
    }

    @Test
    void updateSpeakingSection_explicitSameMaterialTitle_repairsRootDriftAndPublishesTitles() {
        Material material = editableMaterial("Current Title", "Stale Root Title");
        MaterialNode root = material.getRoot();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .materialTitle("Current Title")
                .build());

        assertAll(
                () -> assertThat(material.getTitle()).isEqualTo("Current Title"),
                () -> assertThat(root.getTitle()).as("repaired root title").isEqualTo("Current Title"),
                () -> assertThat(material.getVersion()).as("material version").isEqualTo(2L),
                () -> assertThat(root.getVersion()).as("root version").isEqualTo(4L),
                () -> assertThat(material.getUpdatedAt()).as("material updatedAt").isAfter(ORIGINAL_TIME),
                () -> assertThat(root.getUpdatedAt()).as("root updatedAt").isAfter(ORIGINAL_TIME),
                () -> {
                    var materialCaptor = forClass(Material.class);
                    verify(materialRepository, times(1)).save(materialCaptor.capture());
                    assertThat(materialCaptor.getValue()).isSameAs(material);
                    assertThat(materialCaptor.getValue().getRoot()).isSameAs(root);
                },
                () -> {
                    assertThat(root.childAt(0).getTitle()).isEqualTo("Part 1");
                    assertThat(root.childAt(1).getTitle()).isEqualTo("Part 2");
                    for (MaterialNode part : root.getChildren()) {
                        assertThat(part.getVersion()).isEqualTo(3L);
                        assertThat(part.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
                        assertThat(part.childAt(0).getVersion()).isEqualTo(3L);
                        assertThat(part.childAt(0).getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
                    }
                },
                () -> {
                    var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
                    verify(outboxPort, times(1)).append(any(), eq("Material"), eq(material.getId().toString()),
                            eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
                    MaterialDetailsUpsertedEvent event = eventCaptor.getValue();
                    assertAll(
                            () -> assertThat(event.getMaterialId()).isEqualTo(material.getId()),
                            () -> assertThat(event.getVersion()).isEqualTo(2L),
                            () -> assertThat(event.getMaterialTitle()).isEqualTo("Current Title"),
                            () -> assertThat(event.getPart1Title()).isEqualTo(root.childAt(0).getTitle()),
                            () -> assertThat(event.getPart2Title()).isEqualTo(root.childAt(1).getTitle()),
                            () -> assertThat(event.getDescription()).isNull(),
                            () -> assertThat(event.getUpdatedAt()).isNotNull()
                    );
                }
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Old Material", "  Old Material  "})
    void updateSpeakingSection_synchronizedMaterialTitle_isNoOp(String title) {
        Material material = editableMaterial();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId()).materialTitle(title).build());

        assertThat(material.getTitle()).isEqualTo("Old Material");
        assertThat(material.getRoot().getTitle()).isEqualTo("Old Material");
        assertThat(material.getVersion()).isEqualTo(1L);
        assertThat(material.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(material.getRoot().getVersion()).isEqualTo(3L);
        assertThat(material.getRoot().getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        verify(materialRepository, never()).save(any());
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Old Material", "  Old Material  ", "", "  "})
    void updateSpeakingSection_descriptionOnly_savesActualChangeWithoutTitleEvent(String title) {
        Material material = editableMaterial();
        var command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .materialTitle(title)
                .materialDescription("  New description  ")
                .build();

        service.updateSpeakingSection(command);

        assertThat(material.getDescription()).isEqualTo("New description");
        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getUpdatedAt()).isAfter(ORIGINAL_TIME);
        assertThat(material.getTitle()).isEqualTo("Old Material");
        assertThat(material.getRoot().getTitle()).isEqualTo("Old Material");
        assertThat(material.getRoot().getVersion()).isEqualTo(3L);
        assertThat(material.getRoot().getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        verify(materialRepository, times(1)).save(material);
        Instant updatedAt = material.getUpdatedAt();

        // Repeating the padded description is a domain no-op and must not save again.
        service.updateSpeakingSection(command);

        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getUpdatedAt()).isEqualTo(updatedAt);
        verify(materialRepository, times(1)).save(material);
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t\n"})
    void updateSpeakingSection_omittedMaterialTitle_doesNotRepairDrift(String title) {
        Material material = editableMaterial("Current Title", "Stale Root Title");

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId()).materialTitle(title).build());

        assertThat(material.getTitle()).isEqualTo("Current Title");
        assertThat(material.getRoot().getTitle()).isEqualTo("Stale Root Title");
        assertThat(material.getVersion()).isEqualTo(1L);
        assertThat(material.getRoot().getVersion()).isEqualTo(3L);
        assertThat(material.getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        assertThat(material.getRoot().getUpdatedAt()).isEqualTo(ORIGINAL_TIME);
        verify(materialRepository, never()).save(any());
        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void updateSpeakingSection_titleAndDescription_versionsOnceAndPublishesFinalDetails() {
        Material material = editableMaterial();

        service.updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(material.getId())
                .materialTitle("  New Material  ")
                .materialDescription("  New description  ")
                .build());

        assertThat(material.getTitle()).isEqualTo("New Material");
        assertThat(material.getRoot().getTitle()).isEqualTo("New Material");
        assertThat(material.getDescription()).isEqualTo("New description");
        assertThat(material.getVersion()).isEqualTo(2L);
        assertThat(material.getRoot().getVersion()).isEqualTo(4L);
        verify(materialRepository, times(1)).save(material);
        var eventCaptor = forClass(MaterialDetailsUpsertedEvent.class);
        verify(outboxPort, times(1)).append(any(), eq("Material"), eq(material.getId().toString()),
                eq(MATERIAL_DETAILS_UPSERTED), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getVersion()).isEqualTo(2L);
        assertThat(eventCaptor.getValue().getMaterialTitle()).isEqualTo("New Material");
        assertThat(eventCaptor.getValue().getDescription()).isEqualTo("New description");
    }

    private Material editableMaterial() {
        return editableMaterial("Old Material", "Old Material");
    }

    private Material editableMaterial(String materialTitle, String rootTitle) {
        Material material = Material.builder().id(906L).title(materialTitle)
                .version(1L).updatedAt(ORIGINAL_TIME).build();
        MaterialNode root = MaterialNode.builder().id(960L).materialId(material.getId())
                .title(rootTitle).version(3L).updatedAt(ORIGINAL_TIME).build();
        for (int index = 0; index < 2; index++) {
            MaterialNode part = MaterialNode.builder().id(961L + index).materialId(material.getId())
                    .parentNodeId(root.getId()).displayOrder(index).title("Part " + (index + 1))
                    .version(3L).updatedAt(ORIGINAL_TIME).build();
            MaterialNode question = MaterialNode.builder().id(963L + index).materialId(material.getId())
                    .parentNodeId(part.getId()).displayOrder(0).transcriptText("Original transcript")
                    .config(Map.of("responseTimeSeconds", 30)).version(3L).updatedAt(ORIGINAL_TIME).build();
            part.addChild(question);
            root.addChild(part);
        }
        material.attachRoot(root);
        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));
        return material;
    }

    @Test
    void updateSpeakingSection_onlyDescriptionChanged_doesNotAppendOutboxEvent() {
        Long materialId = 902L;
        Long rootNodeId = 920L;
        Long part1NodeId = 921L;

        Material material = Material.builder().id(materialId).title("Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).title("Part 1").version(1L).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .materialDescription("Only description update")
                .build();

        service.updateSpeakingSection(command);

        verify(outboxPort, never()).append(any(), any(), any(), any(), any());
    }

    @Test
    void publishSpeakingSection_whenSectionIsComplete_marksMaterialAsPublished() {
        Long materialId = 1001L;
        Long rootNodeId = 2001L;
        Long part1NodeId = 2002L;
        Long part2NodeId = 2003L;
        Long part1Question1Id = 2101L;
        Long part1Question2Id = 2102L;
        Long part1Question3Id = 2103L;
        Long part1Question4Id = 2104L;
        Long part1Question5Id = 2105L;
        Long part1Question6Id = 2106L;
        Long part1Question7Id = 2107L;
        Long part2Question1Id = 2201L;
        Long part2Question2Id = 2202L;
        Long part2Question3Id = 2203L;
        Long part2Question4Id = 2204L;

        Material material = Material.builder()
                .id(materialId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(2L)
                .build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());

        MaterialNode root = MaterialNode.builder()
                .id(rootNodeId)
                .materialId(materialId)
                .kind(MaterialNodeKind.SECTION)
                .title("Complete speaking section")
                .build();

        MaterialNode part1 = MaterialNode.builder()
                .id(part1NodeId)
                .materialId(materialId)
                .parentNodeId(rootNodeId)
                .displayOrder(0)
                .kind(MaterialNodeKind.PART)
                .title("Part 1")
                .build();

        MaterialNode part2 = MaterialNode.builder()
                .id(part2NodeId)
                .materialId(materialId)
                .parentNodeId(rootNodeId)
                .displayOrder(1)
                .kind(MaterialNodeKind.PART)
                .title("Part 2")
                .build();

        List<MaterialNode> part1Questions = List.of(
                questionNode(materialId, part1Question1Id, part1NodeId, 0, "P1 Q1"),
                questionNode(materialId, part1Question2Id, part1NodeId, 1, "P1 Q2"),
                questionNode(materialId, part1Question3Id, part1NodeId, 2, "P1 Q3"),
                questionNode(materialId, part1Question4Id, part1NodeId, 3, "P1 Q4"),
                questionNode(materialId, part1Question5Id, part1NodeId, 4, "P1 Q5"),
                questionNode(materialId, part1Question6Id, part1NodeId, 5, "P1 Q6"),
                questionNode(materialId, part1Question7Id, part1NodeId, 6, "P1 Q7")
        );

        List<MaterialNode> part2Questions = List.of(
                questionNode(materialId, part2Question1Id, part2NodeId, 0, "P2 Q1"),
                questionNode(materialId, part2Question2Id, part2NodeId, 1, "P2 Q2"),
                questionNode(materialId, part2Question3Id, part2NodeId, 2, "P2 Q3"),
                questionNode(materialId, part2Question4Id, part2NodeId, 3, "P2 Q4")
        );

        part1Questions.forEach(question -> question.addAsset(audioAsset(question.getId())));
        part2Questions.forEach(question -> question.addAsset(audioAsset(question.getId())));
        part1.addAsset(imageAsset(part1NodeId));
        part1Questions.forEach(part1::addChild);
        part2Questions.forEach(part2::addChild);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.publishSpeakingSection(materialId);

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());

        Material savedMaterial = materialCaptor.getValue();
        assertThat(savedMaterial.getStatus()).isEqualTo(MaterialStatus.PUBLISHED);
        assertThat(savedMaterial.getVersion()).isEqualTo(3L);
        assertThat(savedMaterial.getUpdatedAt()).isNotNull();
    }

    @Test
    void publishSpeakingSection_whenMaterialTitleIsMissing_throwsException() {
        Long materialId = 3001L;
        Long rootNodeId = 3002L;

        Material material = Material.builder()
                .id(materialId)
                .title(" ")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.publishSpeakingSection(materialId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("material title is required");

        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    void publishSpeakingSection_whenPart1ImageIsMissing_throwsException() {
        Long materialId = 3003L;
        Long rootNodeId = 3004L;
        Long part1NodeId = 3005L;
        Long part2NodeId = 3006L;
        Long part1QuestionId = 3007L;

        Material material = Material.builder()
                .id(materialId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());

        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).kind(MaterialNodeKind.SECTION).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).kind(MaterialNodeKind.PART).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(1).kind(MaterialNodeKind.PART).title("Part 2").build();

        MaterialNode part1Question = questionNode(materialId, part1QuestionId, part1NodeId, 0, "P1 Q1");
        MaterialNode part2Question1 = questionNode(materialId, 3008L, part2NodeId, 0, "P2 Q1");
        MaterialNode part2Question2 = questionNode(materialId, 3009L, part2NodeId, 1, "P2 Q2");
        MaterialNode part2Question3 = questionNode(materialId, 3010L, part2NodeId, 2, "P2 Q3");
        MaterialNode part2Question4 = questionNode(materialId, 3011L, part2NodeId, 3, "P2 Q4");

        part1.addChild(part1Question);
        part2.addChild(part2Question1);
        part2.addChild(part2Question2);
        part2.addChild(part2Question3);
        part2.addChild(part2Question4);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.publishSpeakingSection(materialId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Part 1 image is required");

        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    void publishSpeakingSection_whenPart1QuestionAudioIsMissing_throwsException() {
        Long materialId = 3012L;
        Long rootNodeId = 3013L;
        Long part1NodeId = 3014L;
        Long part2NodeId = 3015L;
        Long part1QuestionId = 3016L;

        Material material = Material.builder()
                .id(materialId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());

        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).kind(MaterialNodeKind.SECTION).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).kind(MaterialNodeKind.PART).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(1).kind(MaterialNodeKind.PART).title("Part 2").build();

        List<MaterialNode> part1Questions = List.of(
                questionNode(materialId, part1QuestionId, part1NodeId, 0, "P1 Q1"),
                questionNode(materialId, 3021L, part1NodeId, 1, "P1 Q2"),
                questionNode(materialId, 3022L, part1NodeId, 2, "P1 Q3"),
                questionNode(materialId, 3023L, part1NodeId, 3, "P1 Q4"),
                questionNode(materialId, 3024L, part1NodeId, 4, "P1 Q5"),
                questionNode(materialId, 3025L, part1NodeId, 5, "P1 Q6"),
                questionNode(materialId, 3026L, part1NodeId, 6, "P1 Q7")
        );
        for (int i = 1; i < part1Questions.size(); i++) {
            part1Questions.get(i).addAsset(audioAsset(part1Questions.get(i).getId()));
        }

        List<MaterialNode> part2Questions = List.of(
                questionNode(materialId, 3017L, part2NodeId, 0, "P2 Q1"),
                questionNode(materialId, 3018L, part2NodeId, 1, "P2 Q2"),
                questionNode(materialId, 3019L, part2NodeId, 2, "P2 Q3"),
                questionNode(materialId, 3020L, part2NodeId, 3, "P2 Q4")
        );
        part2Questions.forEach(question -> question.addAsset(audioAsset(question.getId())));

        part1.addAsset(imageAsset(part1NodeId));
        part1Questions.forEach(part1::addChild);
        part2Questions.forEach(part2::addChild);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.publishSpeakingSection(materialId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Part 1 question 0 is missing audio");

        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    void publishSpeakingSection_whenPart2QuestionCountIsInvalid_throwsException() {
        Long materialId = 3021L;
        Long rootNodeId = 3022L;
        Long part1NodeId = 3023L;
        Long part2NodeId = 3024L;
        Long part1QuestionId = 3025L;

        Material material = Material.builder()
                .id(materialId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());

        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).kind(MaterialNodeKind.SECTION).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).kind(MaterialNodeKind.PART).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(1).kind(MaterialNodeKind.PART).title("Part 2").build();

        List<MaterialNode> part1Questions = List.of(
                questionNode(materialId, part1QuestionId, part1NodeId, 0, "P1 Q1"),
                questionNode(materialId, 3026L, part1NodeId, 1, "P1 Q2"),
                questionNode(materialId, 3027L, part1NodeId, 2, "P1 Q3"),
                questionNode(materialId, 3028L, part1NodeId, 3, "P1 Q4"),
                questionNode(materialId, 3029L, part1NodeId, 4, "P1 Q5"),
                questionNode(materialId, 3030L, part1NodeId, 5, "P1 Q6"),
                questionNode(materialId, 3031L, part1NodeId, 6, "P1 Q7")
        );
        part1Questions.forEach(question -> question.addAsset(audioAsset(question.getId())));

        List<MaterialNode> part2Questions = List.of(
                questionNode(materialId, 3032L, part2NodeId, 0, "P2 Q1"),
                questionNode(materialId, 3033L, part2NodeId, 1, "P2 Q2"),
                questionNode(materialId, 3034L, part2NodeId, 2, "P2 Q3")
        );
        part2Questions.forEach(question -> question.addAsset(audioAsset(question.getId())));

        part1.addAsset(imageAsset(part1NodeId));
        part1Questions.forEach(part1::addChild);
        part2Questions.forEach(part2::addChild);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> service.publishSpeakingSection(materialId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Part 2 must have exactly 4 questions");

        verify(materialRepository, never()).save(any(Material.class));
    }

    private static MaterialNode questionNode(Long materialId, Long id, Long parentNodeId, int displayOrder, String transcriptText) {
        return MaterialNode.builder()
                .id(id)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind(MaterialNodeKind.ITEM)
                .displayOrder(displayOrder)
                .transcriptText(transcriptText)
                .build();
    }

    private static Material persistedSpeakingScaffoldMaterial(Long materialId, String title, String description) {
        Material material = Material.builder()
                .id(materialId)
                .examFamilyId(1L)
                .title(title)
                .description(description)
                .status(MaterialStatus.DRAFT)
                .version(0L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();

        MaterialNode root = scaffoldNode(materialId, 9100L, null, MaterialNodeKind.SECTION, title, 0, null, Map.of());
        MaterialNode part1 = scaffoldNode(materialId, 9101L, root.getId(), MaterialNodeKind.PART, "Part 1", 0, null, Map.of());
        MaterialNode part2 = scaffoldNode(materialId, 9102L, root.getId(), MaterialNodeKind.PART, "Part 2", 1, null, Map.of());

        part1.addChild(scaffoldNode(materialId, 9110L, part1.getId(), MaterialNodeKind.ITEM, "Question 1", 0,
                "Part 1 question 1", Map.of("prepTimeSeconds", 15)));
        for (int i = 1; i < 7; i++) {
            part1.addChild(scaffoldNode(materialId, 9110L + i, part1.getId(), MaterialNodeKind.ITEM,
                    "Question " + (i + 1), i, null, Map.of()));
        }

        part2.addChild(scaffoldNode(materialId, 9120L, part2.getId(), MaterialNodeKind.ITEM, "Question 1", 0,
                "Part 2 question 1", Map.of()));
        part2.addChild(scaffoldNode(materialId, 9121L, part2.getId(), MaterialNodeKind.ITEM, "Question 2", 1,
                "Part 2 question 2", Map.of("prepTimeSeconds", 30)));
        part2.addChild(scaffoldNode(materialId, 9122L, part2.getId(), MaterialNodeKind.ITEM, "Question 3", 2,
                null, Map.of()));
        part2.addChild(scaffoldNode(materialId, 9123L, part2.getId(), MaterialNodeKind.ITEM, "Question 4", 3,
                null, Map.of()));

        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);
        return material;
    }

    private static Material persistedSpeakingScaffoldMaterialWithInitialAssets(Long materialId, String title, String description) {
        Material material = persistedSpeakingScaffoldMaterial(materialId, title, description);
        material.addNodeAsset(9101L, MaterialAsset.Kind.IMAGE,
                "speaking/9100/part1/image/image.png", "cover.png", "image/png", 3L);
        material.addNodeAsset(9110L, MaterialAsset.Kind.AUDIO,
                "speaking/9100/part1/audio/question_1.mp3", "part1-question1.mp3", "audio/mpeg", 11L);
        material.addNodeAsset(9120L, MaterialAsset.Kind.AUDIO,
                "speaking/9100/part2/audio/question_1.mp3", "part2-question1.mp3", "audio/mpeg", 12L);
        return material;
    }

    private static MaterialNode scaffoldNode(
            Long materialId,
            Long id,
            Long parentNodeId,
            MaterialNodeKind kind,
            String title,
            int displayOrder,
            String transcriptText,
            Map<String, Object> config
    ) {
        return MaterialNode.builder()
                .id(id)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind(kind)
                .title(title)
                .displayOrder(displayOrder)
                .skillId(4L)
                .transcriptText(transcriptText)
                .responseMode(kind == MaterialNodeKind.ITEM ? "SPOKEN" : "NONE")
                .responseRequired(kind == MaterialNodeKind.ITEM)
                .scoringMode("NONE")
                .config(config)
                .version(0L)
                .createdAt(ORIGINAL_TIME)
                .updatedAt(ORIGINAL_TIME)
                .build();
    }

    private static MaterialAsset imageAsset(Long nodeId) {
        return MaterialAsset.builder()
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/1001/part1/image/image.png")
                .build();
    }

    private static MaterialAsset imageAsset(
            Long assetId,
            Long nodeId,
            String storageKey,
            String originalFilename,
            Long fileSizeBytes,
            Long version
    ) {
        return MaterialAsset.builder()
                .id(assetId)
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey(storageKey)
                .originalFilename(originalFilename)
                .mimeType("image/png")
                .fileSizeBytes(fileSizeBytes)
                .version(version)
                .build();
    }

    private static MaterialAsset audioAsset(Long nodeId) {
        return MaterialAsset.builder()
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/1001/part1/audio/question_1.mp3")
                .build();
    }

    private static MaterialAsset audioAsset(
            Long assetId,
            Long nodeId,
            String storageKey,
            String originalFilename,
            Long fileSizeBytes,
            Long version
    ) {
        return MaterialAsset.builder()
                .id(assetId)
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey(storageKey)
                .originalFilename(originalFilename)
                .mimeType("audio/mpeg")
                .fileSizeBytes(fileSizeBytes)
                .version(version)
                .build();
    }

    private static void assertImmutableImageReplacementKey(String replacementKey, Long materialId, int partNumber, String oldKey) {
        assertThat(replacementKey)
                .isNotEqualTo(oldKey)
                .startsWith("speaking/" + materialId + "/part" + partNumber + "/image/")
                .endsWith(".png")
                .doesNotContain("old-image");
    }

    private static void assertImmutableAudioReplacementKey(
            String replacementKey,
            Long materialId,
            int partNumber,
            int questionNumber,
            String oldKey
    ) {
        assertThat(replacementKey)
                .isNotEqualTo(oldKey)
                .startsWith("speaking/" + materialId + "/part" + partNumber + "/audio/question_" + questionNumber + "/")
                .endsWith(".mp3")
                .doesNotContain("old-question");
    }

    private static final class TestUuidSequenceSupplier implements Supplier<UUID> {
        private final Deque<UUID> values;

        private TestUuidSequenceSupplier(String... values) {
            this.values = new LinkedList<>();
            for (String value : values) {
                this.values.add(UUID.fromString(value));
            }
        }

        @Override
        public UUID get() {
            UUID next = values.pollFirst();
            if (next == null) {
                throw new IllegalStateException("No test UUIDs remaining");
            }
            return next;
        }
    }

}

