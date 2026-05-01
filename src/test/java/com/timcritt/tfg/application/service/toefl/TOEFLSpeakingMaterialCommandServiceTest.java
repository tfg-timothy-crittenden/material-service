package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.MaterialDeletedEvent;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialDeletionEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TOEFLSpeakingMaterialCommandServiceTest {

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final MaterialNodeRepositoryPort materialNodeRepository = mock(MaterialNodeRepositoryPort.class);
    private final MaterialAssetRepositoryPort materialAssetRepository = mock(MaterialAssetRepositoryPort.class);
    private final StorageRepositoryPort storageRepositoryPort = mock(StorageRepositoryPort.class);
    private final MaterialDeletionEventPublisherPort deletionEventPublisher = mock(MaterialDeletionEventPublisherPort.class);

    private final TOEFLSpeakingMaterialCommandService service = new TOEFLSpeakingMaterialCommandService(
            materialRepository,
            materialNodeRepository,
            materialAssetRepository,
            storageRepositoryPort,
            deletionEventPublisher
    );

    @Test
    void uploadSpeakingSection_scaffoldsMissingDraftQuestionNodesForBothParts() {
        AtomicLong nodeIds = new AtomicLong(100L);
        AtomicLong materialIds = new AtomicLong(1000L);
        List<MaterialNode> savedNodes = new ArrayList<>();

        when(materialNodeRepository.save(any(MaterialNode.class))).thenAnswer(invocation -> {
            MaterialNode node = invocation.getArgument(0);
            if (node.getId() == null) {
                node.setId(nodeIds.getAndIncrement());
            }
            savedNodes.add(MaterialNode.builder()
                    .id(node.getId())
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
            return material;
        });

        Long materialId = service.uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand.builder()
                .materialTitle("Draft section")
                .partTitle("Part 1")
                .questions(List.of(question("Part 1 question 1")))
                .part2Title("Part 2")
                .part2Questions(List.of(
                        question("Part 2 question 1"),
                        question("Part 2 question 2")
                ))
                .build());

        assertThat(materialId).isEqualTo(1000L);

        MaterialNode root = savedNodes.stream()
                .filter(node -> "SECTION".equals(node.getKind()))
                .findFirst()
                .orElseThrow();

        MaterialNode part1 = savedNodes.stream()
                .filter(node -> root.getId().equals(node.getParentNodeId()) && node.getDisplayOrder() == 0)
                .findFirst()
                .orElseThrow();

        MaterialNode part2 = savedNodes.stream()
                .filter(node -> root.getId().equals(node.getParentNodeId()) && node.getDisplayOrder() == 1)
                .findFirst()
                .orElseThrow();

        List<MaterialNode> part1Questions = savedNodes.stream()
                .filter(node -> part1.getId().equals(node.getParentNodeId()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();

        List<MaterialNode> part2Questions = savedNodes.stream()
                .filter(node -> part2.getId().equals(node.getParentNodeId()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();

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
        verify(deletionEventPublisher, times(1)).publishMaterialDeleted(any(MaterialDeletedEvent.class));
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
        imageAsset.setStorageKey("speaking/part1/image_old.png");
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

        verify(storageRepositoryPort, times(1)).uploadObject(any(), any(), any());
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/part1/image_old.png");
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
        imageAsset.setStorageKey("speaking/part1/remove-me.png");

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
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/part1/remove-me.png");
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
        audioAsset.setStorageKey("speaking/part1/audio/remove-me.mp3");

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
        verify(storageRepositoryPort, times(1)).deleteObject("toefl", "speaking/part1/audio/remove-me.mp3");
    }
}

