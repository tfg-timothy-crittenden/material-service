package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.port.outbound.*;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DELETED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TOEFLSpeakingMaterialCommandServiceTest {

    private static final Instant ORIGINAL_TIME = Instant.parse("2020-01-01T00:00:00Z");

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final MaterialNodeRepositoryPort materialNodeRepository = mock(MaterialNodeRepositoryPort.class);
    private final MaterialAssetRepositoryPort materialAssetRepository = mock(MaterialAssetRepositoryPort.class);
    private final StorageRepositoryPort storageRepositoryPort = mock(StorageRepositoryPort.class);
    private final IntegrationEventOutboxPort outboxPort = mock(IntegrationEventOutboxPort.class);

    private final TOEFLSpeakingMaterialCommandService service = new TOEFLSpeakingMaterialCommandService(
            materialRepository,
            materialNodeRepository,
            materialAssetRepository,
            storageRepositoryPort,
            outboxPort
    );

    @Test
    void uploadSpeakingSection_scaffoldsMissingDraftQuestionNodesForBothParts() {
        AtomicLong nodeIds = new AtomicLong(100L);
        AtomicLong materialIds = new AtomicLong(1000L);
        List<MaterialNode> savedNodes = new ArrayList<>();
        List<Material> savedMaterials = new ArrayList<>();

        // AtomicLong gives the lambda a mutable id counter (local variables captured by lambdas must be effectively final).
        when(materialNodeRepository.save(any(MaterialNode.class))).thenAnswer(invocation -> {
            MaterialNode node = invocation.getArgument(0);

            if (node.getId() == null) {
                node = MaterialNode.builder()
                        .id(nodeIds.getAndIncrement())
                        .materialId(node.getMaterialId())
                        .parentNodeId(node.getParentNodeId())
                        .kind(node.getKind())
                        .title(node.getTitle())
                        .displayOrder(node.getDisplayOrder())
                        .skillId(node.getSkillId())
                        .transcriptText(node.getTranscriptText())
                        .responseMode(node.getResponseMode())
                        .responseRequired(node.getResponseRequired())
                        .scoringMode(node.getScoringMode())
                        .config(node.getConfig())
                        .version(node.getVersion())
                        .createdAt(node.getCreatedAt())
                        .updatedAt(node.getUpdatedAt())
                        .build();
            }

            savedNodes.add(node);
            return node;
        });

        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            if (material.getId() == null) {
                Material created = Material.builder()
                        .id(materialIds.getAndIncrement())
                        .examFamilyId(material.getExamFamilyId())
                        .title(material.getTitle())
                        .description(material.getDescription())
                        .authorId(material.getAuthorId())
                        .ownerOrgId(material.getOwnerOrgId())
                        .status(material.getStatus())
                        .version(material.getVersion())
                        .createdAt(material.getCreatedAt())
                        .updatedAt(material.getUpdatedAt())
                        .build();
                if (material.getRoot() != null) {
                    created.attachRoot(material.getRoot());
                }
                material = created;
            }
            Material savedMaterial = Material.builder()
                    .id(material.getId())
                    .examFamilyId(material.getExamFamilyId())
                    .title(material.getTitle())
                    .description(material.getDescription())
                    .status(material.getStatus())
                    .version(material.getVersion())
                    .createdAt(material.getCreatedAt())
                    .updatedAt(material.getUpdatedAt())
                    .build();
            if (material.getRoot() != null) {
                savedMaterial.attachRoot(material.getRoot());
            }
            savedMaterials.add(savedMaterial);
            return material;
        });

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

        assertThat(materialId).isEqualTo(1000L);
        assertThat(savedMaterials).hasSize(2);
        assertThat(savedMaterials.getFirst().getRoot()).isNull();

        var storageKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(4)).uploadObject(eq("toefl"), storageKeyCaptor.capture(), any());
        assertThat(storageKeyCaptor.getAllValues()).containsExactly(
                "speaking/1000/part1/image/image.png",
                "speaking/1000/part1/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_2.mp3"
        );

        MaterialNode root = savedNodes.stream()
                .filter(node -> node.getKind() == MaterialNodeKind.SECTION)
                .findFirst()
                .orElseThrow();

        assertThat(root.getMaterialId()).isEqualTo(materialId);
        assertThat(savedMaterials.get(1).getRootId()).isEqualTo(root.getId());

        MaterialNode part1 = savedNodes.stream()
                .filter(node -> root.getId().equals(node.getParentNodeId()) && node.getDisplayOrder() == 0)
                .findFirst()
                .orElseThrow();

        assertThat(part1.getMaterialId()).isEqualTo(materialId);

        MaterialNode part2 = savedNodes.stream()
                .filter(node -> root.getId().equals(node.getParentNodeId()) && node.getDisplayOrder() == 1)
                .findFirst()
                .orElseThrow();

        assertThat(part2.getMaterialId()).isEqualTo(materialId);

        List<MaterialNode> part1Questions = savedNodes.stream()
                .filter(node -> part1.getId().equals(node.getParentNodeId()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();

        assertThat(part1Questions).extracting(MaterialNode::getMaterialId).containsOnly(materialId);

        List<MaterialNode> part2Questions = savedNodes.stream()
                .filter(node -> part2.getId().equals(node.getParentNodeId()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();

        assertThat(part2Questions).extracting(MaterialNode::getMaterialId).containsOnly(materialId);

        assertThat(part1Questions)
                .extracting(MaterialNode::getDisplayOrder, MaterialNode::getTranscriptText)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(0, "Part 1 question 1"),
                        org.assertj.core.groups.Tuple.tuple(1, null),
                        org.assertj.core.groups.Tuple.tuple(2, null),
                        org.assertj.core.groups.Tuple.tuple(3, null),
                        org.assertj.core.groups.Tuple.tuple(4, null),
                        org.assertj.core.groups.Tuple.tuple(5, null),
                        org.assertj.core.groups.Tuple.tuple(6, null)
                );

        assertThat(part2Questions)
                .extracting(MaterialNode::getDisplayOrder, MaterialNode::getTranscriptText)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(0, "Part 2 question 1"),
                        org.assertj.core.groups.Tuple.tuple(1, "Part 2 question 2"),
                        org.assertj.core.groups.Tuple.tuple(2, null),
                        org.assertj.core.groups.Tuple.tuple(3, null)
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
    void deleteSpeakingSection_deletesTreeAndCleansStorageKeys() {
        Long materialId = 77L;
        Long rootNodeId = 100L;
        Long childNodeId = 101L;

        Material material = Material.builder().id(materialId).build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(
                MaterialNode.builder().id(childNodeId).parentNodeId(rootNodeId).build()
        ));
        when(materialNodeRepository.findByParentNodeId(childNodeId)).thenReturn(List.of());

        MaterialAsset rootAsset = MaterialAsset.builder()
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/root-audio.mp3")
                .build();
        MaterialAsset childAsset = MaterialAsset.builder()
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/child-audio.mp3")
                .build();

        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of(rootAsset));
        when(materialAssetRepository.findByMaterialNodeId(childNodeId)).thenReturn(List.of(childAsset));

        service.deleteSpeakingSection(materialId);

        verify(materialNodeRepository, times(1)).deleteById(rootNodeId);
        verify(materialRepository, times(1)).delete(materialId);
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/root-audio.mp3");
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/child-audio.mp3");

        var eventIdCaptor = forClass(java.util.UUID.class);
        var aggregateTypeCaptor = forClass(String.class);
        var aggregateIdCaptor = forClass(String.class);
        var eventTypeCaptor = forClass(String.class);
        var payloadCaptor = forClass(MaterialDeletedEvent.class);

        verify(outboxPort, times(1)).append(
                eventIdCaptor.capture(),
                aggregateTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                eventTypeCaptor.capture(),
                payloadCaptor.capture());

        assertThat(eventIdCaptor.getValue()).isNotNull();
        assertThat(aggregateTypeCaptor.getValue()).isEqualTo("Material");
        assertThat(aggregateIdCaptor.getValue()).isEqualTo(materialId.toString());
        assertThat(eventTypeCaptor.getValue()).isEqualTo(MATERIAL_DELETED);
        assertThat(payloadCaptor.getValue().getMaterialId()).isEqualTo(materialId);
        assertThat(payloadCaptor.getValue().getRootNodeId()).isEqualTo(rootNodeId);
        assertThat(payloadCaptor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void updateSpeakingSection_replacesAssetAndDeletesOldStorageKey() {
        Long materialId = 88L;
        Long rootNodeId = 200L;
        Long part1NodeId = 201L;

        Material material = Material.builder().id(materialId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());

        MaterialAsset imageAsset = MaterialAsset.builder()
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/88/part1/image/old-image.png")
                .version(3L)
                .build();

        List<MaterialAsset> part1Assets = new ArrayList<>(List.of(imageAsset));

        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenAnswer(invocation -> part1Assets);
        when(materialAssetRepository.save(any(MaterialAsset.class))).thenAnswer(invocation -> {
            MaterialAsset saved = invocation.getArgument(0);
            part1Assets.clear();
            part1Assets.add(saved);
            return saved;
        });

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
        assertThat(uploadKeyCaptor.getValue()).isEqualTo("speaking/88/part1/image/image.png");
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/88/part1/image/old-image.png");
    }

    @Test
    void updateSpeakingSection_removePartImage_deletesAssetEntryAndStorageKey() {
        Long materialId = 89L;
        Long rootNodeId = 300L;
        Long part1NodeId = 301L;

        Material material = Material.builder().id(materialId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).materialId(materialId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).materialId(materialId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        root.addChild(part1);
        material.attachRoot(root);

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());

        MaterialAsset imageAsset = MaterialAsset.builder()
                .id(5000L)
                .materialNodeId(part1NodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/89/part1/image/old-image.png")
                .build();

        List<MaterialAsset> part1Assets = new ArrayList<>(List.of(imageAsset));
        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenAnswer(invocation -> new ArrayList<>(part1Assets));
        when(materialAssetRepository.findByMaterialNodeId(302L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(303L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(304L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(305L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(306L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(307L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(308L)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(309L)).thenReturn(List.of());

        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(
                MaterialNode.builder().id(302L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(303L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(304L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(305L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(306L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(307L).parentNodeId(part1NodeId).build(),
                MaterialNode.builder().id(308L).parentNodeId(part1NodeId).build()
        ));
        when(materialNodeRepository.findByParentNodeId(302L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(303L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(304L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(305L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(306L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(307L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(308L)).thenReturn(List.of());

        doAnswer(invocation -> {
            part1Assets.clear();
            return null;
        }).when(materialAssetRepository).deleteById(5000L);

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .removePartImage(true)
                .build();

        service.updateSpeakingSection(command);

        verify(materialAssetRepository, times(1)).deleteById(5000L);
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/89/part1/image/old-image.png");
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

        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(q0));
        when(materialNodeRepository.findByParentNodeId(questionNodeId)).thenReturn(List.of());

        MaterialAsset audioAsset = MaterialAsset.builder()
                .id(6000L)
                .materialNodeId(questionNodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/90/part1/audio/old-question.mp3")
                .build();

        List<MaterialAsset> qAssets = new ArrayList<>(List.of(audioAsset));
        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(questionNodeId)).thenAnswer(invocation -> new ArrayList<>(qAssets));
        doAnswer(invocation -> {
            qAssets.clear();
            return null;
        }).when(materialAssetRepository).deleteById(6000L);

        TOEFLSpeakingSectionUpdateCommand command = TOEFLSpeakingSectionUpdateCommand.builder()
                .materialId(materialId)
                .questions(List.of(
                        SpeakingQuestionPartialUpdateCommand.builder().index(0).removeAudio(true).build()
                ))
                .build();

        service.updateSpeakingSection(command);

        verify(materialAssetRepository, times(1)).deleteById(6000L);
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/90/part1/audio/old-question.mp3");
        assertThat(material.getVersion()).isEqualTo(1L);
        verify(materialRepository, never()).save(any(Material.class));
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
        // Storage scans remain allowed; do not stub separate part-title lookups.
        when(materialNodeRepository.findByParentNodeId(root.getId())).thenReturn(List.of(part1, part2));
        when(materialNodeRepository.findByParentNodeId(part1.getId())).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(part2.getId())).thenReturn(List.of());

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
                () -> assertThat(event.getPart2Title()).isEqualTo(root.childAt(1).getTitle()),
                () -> verify(materialNodeRepository, never()).save(root),
                () -> verify(materialNodeRepository, never()).save(part1),
                () -> verify(materialNodeRepository, never()).save(part2),
                () -> verify(materialNodeRepository, never()).findByParentIdAndDisplayOrder(root.getId(), 0),
                () -> verify(materialNodeRepository, never()).findByParentIdAndDisplayOrder(root.getId(), 1)
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
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialNodeRepository.save(any(MaterialNode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1, part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part2NodeId)).thenReturn(List.of());

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
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialNodeRepository.save(any(MaterialNode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1, part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part2NodeId)).thenReturn(List.of());

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
        // Keep the unrelated storage scans consistent with the loaded tree.
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(question));
        when(materialNodeRepository.findByParentNodeId(questionNodeId)).thenReturn(List.of());

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
                },
                () -> verify(materialNodeRepository, never()).save(argThat(node -> questionNodeId.equals(node.getId()))),
                () -> verify(materialNodeRepository, never()).findByParentIdAndDisplayOrder(part1NodeId, questionIndex)
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
        when(materialNodeRepository.findByParentNodeId(root.getId())).thenReturn(List.of(part1, part2));
        when(materialNodeRepository.findByParentNodeId(part1.getId())).thenReturn(List.of(first, second));
        when(materialNodeRepository.findByParentNodeId(part2.getId())).thenReturn(List.of(third));
        // These reads remain for the existing title-event payload, not update navigation.
        when(materialNodeRepository.findByParentIdAndDisplayOrder(root.getId(), 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(root.getId(), 1)).thenReturn(Optional.of(part2));
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
        verify(materialNodeRepository, never()).findById(root.getId());
        for (MaterialNode question : List.of(first, second, third)) {
            verify(materialNodeRepository, never()).findByParentIdAndDisplayOrder(question.getParentNodeId(), question.getDisplayOrder());
            verify(materialNodeRepository, never()).save(question);
        }
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
                () -> verify(materialNodeRepository, never()).save(any(MaterialNode.class)),
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
        verify(materialNodeRepository, never()).save(any());
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
            when(materialNodeRepository.findByParentNodeId(part.getId())).thenReturn(List.of(question));
        }
        material.attachRoot(root);
        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));
        when(materialNodeRepository.findByParentNodeId(root.getId())).thenReturn(root.getChildren());
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
        when(materialNodeRepository.save(any(MaterialNode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of());

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
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(part1Questions);
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(part2Questions);
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question1Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question2Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question3Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question4Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question5Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question6Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1Question7Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part2Question1Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part2Question2Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part2Question3Id)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part2Question4Id)).thenReturn(List.of(audioAsset()));
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
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(questionNode(materialId, part1QuestionId, part1NodeId, 0, "P1 Q1")));
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of(
                questionNode(materialId, 3008L, part2NodeId, 0, "P2 Q1"),
                questionNode(materialId, 3009L, part2NodeId, 1, "P2 Q2"),
                questionNode(materialId, 3010L, part2NodeId, 2, "P2 Q3"),
                questionNode(materialId, 3011L, part2NodeId, 3, "P2 Q4")
        ));
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1QuestionId)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3008L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3009L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3010L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3011L)).thenReturn(List.of(audioAsset()));

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
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(part1Questions);
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(part2Questions);
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset(part1NodeId)));
        when(materialAssetRepository.findByMaterialNodeId(part1QuestionId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(3021L)).thenReturn(List.of(audioAsset(3021L)));
        when(materialAssetRepository.findByMaterialNodeId(3022L)).thenReturn(List.of(audioAsset(3022L)));
        when(materialAssetRepository.findByMaterialNodeId(3023L)).thenReturn(List.of(audioAsset(3023L)));
        when(materialAssetRepository.findByMaterialNodeId(3024L)).thenReturn(List.of(audioAsset(3024L)));
        when(materialAssetRepository.findByMaterialNodeId(3025L)).thenReturn(List.of(audioAsset(3025L)));
        when(materialAssetRepository.findByMaterialNodeId(3026L)).thenReturn(List.of(audioAsset(3026L)));
        when(materialAssetRepository.findByMaterialNodeId(3017L)).thenReturn(List.of(audioAsset(3017L)));
        when(materialAssetRepository.findByMaterialNodeId(3018L)).thenReturn(List.of(audioAsset(3018L)));
        when(materialAssetRepository.findByMaterialNodeId(3019L)).thenReturn(List.of(audioAsset(3019L)));
        when(materialAssetRepository.findByMaterialNodeId(3020L)).thenReturn(List.of(audioAsset(3020L)));

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
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(part1Questions);
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(part2Questions);
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset(part1NodeId)));
        when(materialAssetRepository.findByMaterialNodeId(part1QuestionId)).thenReturn(List.of(audioAsset(part1QuestionId)));
        when(materialAssetRepository.findByMaterialNodeId(3026L)).thenReturn(List.of(audioAsset(3026L)));
        when(materialAssetRepository.findByMaterialNodeId(3027L)).thenReturn(List.of(audioAsset(3027L)));
        when(materialAssetRepository.findByMaterialNodeId(3028L)).thenReturn(List.of(audioAsset(3028L)));
        when(materialAssetRepository.findByMaterialNodeId(3029L)).thenReturn(List.of(audioAsset(3029L)));
        when(materialAssetRepository.findByMaterialNodeId(3030L)).thenReturn(List.of(audioAsset(3030L)));
        when(materialAssetRepository.findByMaterialNodeId(3031L)).thenReturn(List.of(audioAsset(3031L)));
        when(materialAssetRepository.findByMaterialNodeId(3032L)).thenReturn(List.of(audioAsset(3032L)));
        when(materialAssetRepository.findByMaterialNodeId(3033L)).thenReturn(List.of(audioAsset(3033L)));
        when(materialAssetRepository.findByMaterialNodeId(3034L)).thenReturn(List.of(audioAsset(3034L)));

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

    private static MaterialAsset imageAsset() {
        return MaterialAsset.builder()
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/1001/part1/image/image.png")
                .build();
    }

    private static MaterialAsset imageAsset(Long nodeId) {
        return MaterialAsset.builder()
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.IMAGE)
                .storageKey("speaking/1001/part1/image/image.png")
                .build();
    }

    private static MaterialAsset audioAsset() {
        return MaterialAsset.builder()
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/1001/part1/audio/question_1.mp3")
                .build();
    }

    private static MaterialAsset audioAsset(Long nodeId) {
        return MaterialAsset.builder()
                .materialNodeId(nodeId)
                .kind(MaterialAsset.Kind.AUDIO)
                .storageKey("speaking/1001/part1/audio/question_1.mp3")
                .build();
    }
}

