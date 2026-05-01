package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.SpeakingSectionSummary;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TOEFLSpeakingNavigationUseCaseServiceTest {

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final MaterialNodeRepositoryPort materialNodeRepository = mock(MaterialNodeRepositoryPort.class);
    private final MaterialAssetRepositoryPort materialAssetRepository = mock(MaterialAssetRepositoryPort.class);

    private final TOEFLSpeakingNavigationUseCaseService service = new TOEFLSpeakingNavigationUseCaseService(
            materialRepository,
            materialNodeRepository,
            materialAssetRepository
    );

    @Test
    void getAllSpeakingSectionSummaries_returnsOnlyPublishedMaterials() {
        MaterialNode publishedSection = MaterialNode.builder().id(10L).kind("SECTION").title("Published Section").build();
        MaterialNode draftSection = MaterialNode.builder().id(20L).kind("SECTION").title("Draft Section").build();
        MaterialNode orphanSection = MaterialNode.builder().id(30L).kind("SECTION").title("Orphan Section").build();

        MaterialNode publishedPart1 = MaterialNode.builder().id(11L).parentNodeId(10L).displayOrder(0).title("Part 1").build();
        MaterialNode publishedPart2 = MaterialNode.builder().id(12L).parentNodeId(10L).displayOrder(1).title("Part 2").build();
        MaterialNode draftPart1 = MaterialNode.builder().id(21L).parentNodeId(20L).displayOrder(0).title("Draft Part 1").build();

        Instant createdAt = Instant.parse("2026-05-01T10:15:30Z");
        Instant updatedAt = Instant.parse("2026-05-01T11:15:30Z");

        Material publishedMaterial = Material.builder()
                .id(100L)
                .materialNodeId(10L)
                .title("Published Material")
                .status(MaterialStatus.PUBLISHED)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Material draftMaterial = Material.builder()
                .id(200L)
                .materialNodeId(20L)
                .title("Draft Material")
                .status(MaterialStatus.DRAFT)
                .build();

        when(materialNodeRepository.findByKind("SECTION")).thenReturn(List.of(publishedSection, draftSection, orphanSection));
        when(materialNodeRepository.findByParentNodeId(10L)).thenReturn(List.of(publishedPart1, publishedPart2));
        when(materialNodeRepository.findByParentNodeId(20L)).thenReturn(List.of(draftPart1));
        when(materialNodeRepository.findByParentNodeId(30L)).thenReturn(List.of());
        when(materialRepository.findByMaterialNodeId(10L)).thenReturn(Optional.of(publishedMaterial));
        when(materialRepository.findByMaterialNodeId(20L)).thenReturn(Optional.of(draftMaterial));
        when(materialRepository.findByMaterialNodeId(30L)).thenReturn(Optional.empty());

        List<SpeakingSectionSummary> summaries = service.getAllSpeakingSectionSummaries();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst())
                .extracting(
                        SpeakingSectionSummary::getMaterialId,
                        SpeakingSectionSummary::getSectionId,
                        SpeakingSectionSummary::getSectionTitle,
                        SpeakingSectionSummary::getPart1Id,
                        SpeakingSectionSummary::getPart1Title,
                        SpeakingSectionSummary::getPart2Id,
                        SpeakingSectionSummary::getPart2Title,
                        SpeakingSectionSummary::getStatus,
                        SpeakingSectionSummary::getCreatedAt,
                        SpeakingSectionSummary::getUpdatedAt
                )
                .containsExactly(
                        100L,
                        10L,
                        "Published Section",
                        11L,
                        "Part 1",
                        12L,
                        "Part 2",
                        MaterialStatus.PUBLISHED,
                        createdAt,
                        updatedAt
                );
    }

    @Test
    void getDraftSpeakingSectionSummaries_returnsOnlyDraftMaterials() {
        MaterialNode publishedSection = MaterialNode.builder().id(10L).kind("SECTION").title("Published Section").build();
        MaterialNode draftSection = MaterialNode.builder().id(20L).kind("SECTION").title("Draft Section").build();
        MaterialNode orphanSection = MaterialNode.builder().id(30L).kind("SECTION").title("Orphan Section").build();

        MaterialNode draftPart1 = MaterialNode.builder().id(21L).parentNodeId(20L).displayOrder(0).title("Draft Part 1").build();
        MaterialNode draftPart2 = MaterialNode.builder().id(22L).parentNodeId(20L).displayOrder(1).title("Draft Part 2").build();

        Instant createdAt = Instant.parse("2026-05-01T12:15:30Z");
        Instant updatedAt = Instant.parse("2026-05-01T13:15:30Z");

        Material publishedMaterial = Material.builder()
                .id(100L)
                .materialNodeId(10L)
                .title("Published Material")
                .status(MaterialStatus.PUBLISHED)
                .build();

        Material draftMaterial = Material.builder()
                .id(200L)
                .materialNodeId(20L)
                .title("Draft Material")
                .status(MaterialStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(materialNodeRepository.findByKind("SECTION")).thenReturn(List.of(publishedSection, draftSection, orphanSection));
        when(materialNodeRepository.findByParentNodeId(10L)).thenReturn(List.of());
        when(materialNodeRepository.findByParentNodeId(20L)).thenReturn(List.of(draftPart1, draftPart2));
        when(materialNodeRepository.findByParentNodeId(30L)).thenReturn(List.of());
        when(materialRepository.findByMaterialNodeId(10L)).thenReturn(Optional.of(publishedMaterial));
        when(materialRepository.findByMaterialNodeId(20L)).thenReturn(Optional.of(draftMaterial));
        when(materialRepository.findByMaterialNodeId(30L)).thenReturn(Optional.empty());

        List<SpeakingSectionSummary> summaries = service.getDraftSpeakingSectionSummaries();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst())
                .extracting(
                        SpeakingSectionSummary::getMaterialId,
                        SpeakingSectionSummary::getSectionId,
                        SpeakingSectionSummary::getSectionTitle,
                        SpeakingSectionSummary::getPart1Id,
                        SpeakingSectionSummary::getPart1Title,
                        SpeakingSectionSummary::getPart2Id,
                        SpeakingSectionSummary::getPart2Title,
                        SpeakingSectionSummary::getStatus,
                        SpeakingSectionSummary::getCreatedAt,
                        SpeakingSectionSummary::getUpdatedAt
                )
                .containsExactly(
                        200L,
                        20L,
                        "Draft Section",
                        21L,
                        "Draft Part 1",
                        22L,
                        "Draft Part 2",
                        MaterialStatus.DRAFT,
                        createdAt,
                        updatedAt
                );
    }
}

