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

        Material publishedMaterial = materialWithRoot(100L, 10L, "Published Material", null, MaterialStatus.PUBLISHED, createdAt, updatedAt);

        Material draftMaterial = materialWithRoot(200L, 20L, "Draft Material", null, MaterialStatus.DRAFT, null, null);

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

        Material publishedMaterial = materialWithRoot(100L, 10L, "Published Material", null, MaterialStatus.PUBLISHED, null, null);

        Material draftMaterial = materialWithRoot(200L, 20L, "Draft Material", null, MaterialStatus.DRAFT, createdAt, updatedAt);

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

    @Test
    void getSpeakingSectionForEdit_includesMaterialStatus() {
        Long materialId = 55L;
        Long sectionId = 500L;
        Long part1Id = 501L;

        Material material = materialWithRoot(materialId, sectionId, "Draft Material", "desc", MaterialStatus.DRAFT, null, null);
        MaterialNode section = MaterialNode.builder().id(sectionId).kind("SECTION").title("Draft Material").build();
        MaterialNode part1 = MaterialNode.builder().id(part1Id).parentNodeId(sectionId).displayOrder(0).title("Part 1").build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialNodeRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(sectionId, 0)).thenReturn(Optional.of(part1));
        when(materialNodeRepository.findByParentIdAndDisplayOrder(sectionId, 1)).thenReturn(Optional.empty());
        when(materialNodeRepository.findByParentNodeId(part1Id)).thenReturn(List.of());
        when(materialAssetRepository.findByMaterialNodeId(part1Id)).thenReturn(List.of());

        var result = service.getSpeakingSectionForEdit(materialId);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(MaterialStatus.DRAFT);
    }

    private static Material materialWithRoot(Long materialId, Long rootNodeId, String title, String description, MaterialStatus status, Instant createdAt, Instant updatedAt) {
        Material.Builder builder = Material.builder().id(materialId);
        if (title != null) {
            builder.title(title);
        }
        if (description != null) {
            builder.description(description);
        }
        if (status != null) {
            builder.status(status);
        }
        if (createdAt != null) {
            builder.createdAt(createdAt);
        }
        if (updatedAt != null) {
            builder.updatedAt(updatedAt);
        }
        Material material = builder.build();
        material.attachRoot(MaterialNode.builder().id(rootNodeId).materialId(materialId).build());
        return material;
    }

}

