package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.port.outbound.*;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DELETED;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TOEFLSpeakingMaterialCommandServiceTest {

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
                node.setId(nodeIds.getAndIncrement());
            }
            savedNodes.add(MaterialNode.builder()
                    .id(node.getId())
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
                    .build());
            return node;
        });

        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            if (material.getId() == null) {
                material.setId(materialIds.getAndIncrement());
            }
            savedMaterials.add(Material.builder()
                    .id(material.getId())
                    .materialNodeId(material.getMaterialNodeId())
                    .examFamilyId(material.getExamFamilyId())
                    .title(material.getTitle())
                    .description(material.getDescription())
                    .status(material.getStatus())
                    .version(material.getVersion())
                    .createdAt(material.getCreatedAt())
                    .updatedAt(material.getUpdatedAt())
                    .build());
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
        assertThat(savedMaterials.getFirst().getMaterialNodeId()).isNull();

        var storageKeyCaptor = forClass(String.class);
        verify(storageRepositoryPort, times(4)).uploadObject(eq("toefl"), storageKeyCaptor.capture(), any());
        assertThat(storageKeyCaptor.getAllValues()).containsExactly(
                "speaking/1000/part1/image/image.png",
                "speaking/1000/part1/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_1.mp3",
                "speaking/1000/part2/audio/question_2.mp3"
        );

        MaterialNode root = savedNodes.stream()
                .filter(node -> "SECTION".equals(node.getKind()))
                .findFirst()
                .orElseThrow();

        assertThat(root.getMaterialId()).isEqualTo(materialId);
        assertThat(savedMaterials.get(1).getMaterialNodeId()).isEqualTo(root.getId());

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

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(
                Material.builder().id(materialId).materialNodeId(rootNodeId).build()
        ));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(
                MaterialNode.builder().id(childNodeId).parentNodeId(rootNodeId).build()
        ));
        when(materialNodeRepository.findByParentNodeId(childNodeId)).thenReturn(List.of());

        MaterialAsset rootAsset = new MaterialAsset();
        rootAsset.setStorageKey("speaking/root-audio.mp3");
        MaterialAsset childAsset = new MaterialAsset();
        childAsset.setStorageKey("speaking/child-audio.mp3");

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

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());

        MaterialAsset imageAsset = new MaterialAsset();
        imageAsset.setMaterialNodeId(part1NodeId);
        imageAsset.setKind(MaterialAsset.Kind.IMAGE);
        imageAsset.setStorageKey("speaking/88/part1/image/old-image.png");
        imageAsset.setVersion(3L);

        when(materialAssetRepository.findByMaterialNodeId(rootNodeId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset));
        when(materialAssetRepository.save(any(MaterialAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

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

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of());

        MaterialAsset imageAsset = new MaterialAsset();
        imageAsset.setId(5000L);
        imageAsset.setMaterialNodeId(part1NodeId);
        imageAsset.setKind(MaterialAsset.Kind.IMAGE);
        imageAsset.setStorageKey("speaking/89/part1/image/old-image.png");

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

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Section").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).version(1L).build();
        MaterialNode q0 = MaterialNode.builder().id(questionNodeId).parentNodeId(part1NodeId).displayOrder(0).version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(part1NodeId, 0)).thenReturn(Optional.of(q0));

        when(materialNodeRepository.findByParentNodeId(rootNodeId)).thenReturn(List.of(part1));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(q0));
        when(materialNodeRepository.findByParentNodeId(questionNodeId)).thenReturn(List.of());

        MaterialAsset audioAsset = new MaterialAsset();
        audioAsset.setId(6000L);
        audioAsset.setMaterialNodeId(questionNodeId);
        audioAsset.setKind(MaterialAsset.Kind.AUDIO);
        audioAsset.setStorageKey("speaking/90/part1/audio/old-question.mp3");

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
    }

    @Test
    void updateSpeakingSection_titlesChanged_appendsSingleCompactTitlesEventToOutbox() {
        Long materialId = 901L;
        Long rootNodeId = 910L;
        Long part1NodeId = 911L;
        Long part2NodeId = 912L;

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Old Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Old Part 1").version(1L).build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).parentNodeId(rootNodeId).displayOrder(1).title("Old Part 2").version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
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
        assertThat(event.getVersion()).isEqualTo(2L);
        assertThat(event.getMaterialTitle()).isEqualTo("New Material");
        assertThat(event.getPart1Title()).isEqualTo("New Part 1");
        assertThat(event.getPart2Title()).isEqualTo("New Part 2");
        assertThat(event.getDescription()).isNull();
        assertThat(event.getUpdatedAt()).isNotNull();

        var materialCaptor = forClass(Material.class);
        verify(materialRepository, times(1)).save(materialCaptor.capture());
        assertThat(materialCaptor.getValue().getVersion()).isEqualTo(2L);
    }

    @Test
    void updateSpeakingSection_partTitlesChanged_bumpsMaterialVersionAndAppendsVersionedTitlesEventToOutbox() {
        Long materialId = 903L;
        Long rootNodeId = 930L;
        Long part1NodeId = 931L;
        Long part2NodeId = 932L;

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).title("Old Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Old Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Old Part 1").version(1L).build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).parentNodeId(rootNodeId).displayOrder(1).title("Old Part 2").version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
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
        assertThat(materialCaptor.getValue().getVersion()).isEqualTo(2L);

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
        assertThat(event.getVersion()).isEqualTo(2L);
        assertThat(event.getMaterialTitle()).isEqualTo("Old Material");
        assertThat(event.getPart1Title()).isEqualTo("New Part 1");
        assertThat(event.getPart2Title()).isEqualTo("New Part 2");
        assertThat(event.getDescription()).isNull();
        assertThat(event.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateSpeakingSection_onlyDescriptionChanged_doesNotAppendOutboxEvent() {
        Long materialId = 902L;
        Long rootNodeId = 920L;
        Long part1NodeId = 921L;

        Material material = Material.builder().id(materialId).materialNodeId(rootNodeId).title("Material").version(1L).build();
        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Material").version(1L).build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Part 1").version(1L).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
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
                .materialNodeId(rootNodeId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(2L)
                .build();

        MaterialNode root = MaterialNode.builder()
                .id(rootNodeId)
                .title("Complete speaking section")
                .build();

        MaterialNode part1 = MaterialNode.builder()
                .id(part1NodeId)
                .parentNodeId(rootNodeId)
                .displayOrder(0)
                .title("Part 1")
                .build();

        MaterialNode part2 = MaterialNode.builder()
                .id(part2NodeId)
                .parentNodeId(rootNodeId)
                .displayOrder(1)
                .title("Part 2")
                .build();

        List<MaterialNode> part1Questions = List.of(
                questionNode(part1Question1Id, part1NodeId, 0, "P1 Q1"),
                questionNode(part1Question2Id, part1NodeId, 1, "P1 Q2"),
                questionNode(part1Question3Id, part1NodeId, 2, "P1 Q3"),
                questionNode(part1Question4Id, part1NodeId, 3, "P1 Q4"),
                questionNode(part1Question5Id, part1NodeId, 4, "P1 Q5"),
                questionNode(part1Question6Id, part1NodeId, 5, "P1 Q6"),
                questionNode(part1Question7Id, part1NodeId, 6, "P1 Q7")
        );

        List<MaterialNode> part2Questions = List.of(
                questionNode(part2Question1Id, part2NodeId, 0, "P2 Q1"),
                questionNode(part2Question2Id, part2NodeId, 1, "P2 Q2"),
                questionNode(part2Question3Id, part2NodeId, 2, "P2 Q3"),
                questionNode(part2Question4Id, part2NodeId, 3, "P2 Q4")
        );

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
                .materialNodeId(rootNodeId)
                .title(" ")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();

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
                .materialNodeId(rootNodeId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();

        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).parentNodeId(rootNodeId).displayOrder(1).title("Part 2").build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(questionNode(part1QuestionId, part1NodeId, 0, "P1 Q1")));
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of(
                questionNode(3008L, part2NodeId, 0, "P2 Q1"),
                questionNode(3009L, part2NodeId, 1, "P2 Q2"),
                questionNode(3010L, part2NodeId, 2, "P2 Q3"),
                questionNode(3011L, part2NodeId, 3, "P2 Q4")
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
                .materialNodeId(rootNodeId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();

        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).parentNodeId(rootNodeId).displayOrder(1).title("Part 2").build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(questionNode(part1QuestionId, part1NodeId, 0, "P1 Q1")));
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of(
                questionNode(3017L, part2NodeId, 0, "P2 Q1"),
                questionNode(3018L, part2NodeId, 1, "P2 Q2"),
                questionNode(3019L, part2NodeId, 2, "P2 Q3"),
                questionNode(3020L, part2NodeId, 3, "P2 Q4")
        ));
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1QuestionId)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(3017L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3018L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3019L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3020L)).thenReturn(List.of(audioAsset()));

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
                .materialNodeId(rootNodeId)
                .title("Complete speaking section")
                .status(MaterialStatus.DRAFT)
                .version(1L)
                .build();

        MaterialNode root = MaterialNode.builder().id(rootNodeId).title("Complete speaking section").build();
        MaterialNode part1 = MaterialNode.builder().id(part1NodeId).parentNodeId(rootNodeId).displayOrder(0).title("Part 1").build();
        MaterialNode part2 = MaterialNode.builder().id(part2NodeId).parentNodeId(rootNodeId).displayOrder(1).title("Part 2").build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(root));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, 1)).thenReturn(Optional.of(part2));
        when(materialNodeRepository.findByParentNodeId(part1NodeId)).thenReturn(List.of(questionNode(part1QuestionId, part1NodeId, 0, "P1 Q1")));
        when(materialNodeRepository.findByParentNodeId(part2NodeId)).thenReturn(List.of(
                questionNode(3026L, part2NodeId, 0, "P2 Q1"),
                questionNode(3027L, part2NodeId, 1, "P2 Q2"),
                questionNode(3028L, part2NodeId, 2, "P2 Q3")
        ));
        when(materialAssetRepository.findByMaterialNodeId(part1NodeId)).thenReturn(List.of(imageAsset()));
        when(materialAssetRepository.findByMaterialNodeId(part1QuestionId)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3026L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3027L)).thenReturn(List.of(audioAsset()));
        when(materialAssetRepository.findByMaterialNodeId(3028L)).thenReturn(List.of(audioAsset()));

        assertThatThrownBy(() -> service.publishSpeakingSection(materialId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Part 2 must have exactly 4 questions");

        verify(materialRepository, never()).save(any(Material.class));
    }

    private static MaterialNode questionNode(Long id, Long parentNodeId, int displayOrder, String transcriptText) {
        return MaterialNode.builder()
                .id(id)
                .parentNodeId(parentNodeId)
                .kind("ITEM")
                .displayOrder(displayOrder)
                .transcriptText(transcriptText)
                .build();
    }

    private static MaterialAsset imageAsset() {
        MaterialAsset asset = new MaterialAsset();
        asset.setKind(MaterialAsset.Kind.IMAGE);
        asset.setStorageKey("speaking/1001/part1/image/image.png");
        return asset;
    }

    private static MaterialAsset audioAsset() {
        MaterialAsset asset = new MaterialAsset();
        asset.setKind(MaterialAsset.Kind.AUDIO);
        asset.setStorageKey("speaking/1001/part1/audio/question_1.mp3");
        return asset;
    }
}

