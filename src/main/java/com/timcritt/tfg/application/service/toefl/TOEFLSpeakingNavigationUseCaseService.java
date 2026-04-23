package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialAsset;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TOEFLSpeakingNavigationUseCaseService {
    private final MaterialRepositoryPort materialRepository;
    private final MaterialNodeRepositoryPort materialNodeRepository;
    private final MaterialAssetRepositoryPort materialAssetRepository;

    public TOEFLSpeakingNavigationUseCaseService(
            MaterialRepositoryPort materialRepository,
            MaterialNodeRepositoryPort materialNodeRepository,
            MaterialAssetRepositoryPort materialAssetRepository) {
        this.materialRepository = materialRepository;
        this.materialNodeRepository = materialNodeRepository;
        this.materialAssetRepository = materialAssetRepository;
    }

    public Optional<MaterialNodeWithAssets> getQuestion(Long materialId, int partOrder, int questionOrder) {
        // Convert 1-based index from frontend to 0-based for backend
        int zeroBasedPartOrder = partOrder - 1;
        int zeroBasedQuestionOrder = questionOrder - 1;
        Optional<Material> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isEmpty() || materialOpt.get().getMaterialNodeId() == null) return Optional.empty();
        Long rootNodeId = materialOpt.get().getMaterialNodeId();
        Optional<MaterialNode> partNodeOpt = materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, zeroBasedPartOrder);
        if (partNodeOpt.isEmpty()) return Optional.empty();
        Long partNodeId = partNodeOpt.get().getId();
        Optional<MaterialNode> questionNodeOpt = materialNodeRepository.findByParentIdAndDisplayOrder(partNodeId, zeroBasedQuestionOrder);
        if (questionNodeOpt.isEmpty()) return Optional.empty();
        MaterialNode questionNode = questionNodeOpt.get();
        List<MaterialAsset> assets = materialAssetRepository.findByMaterialNodeId(questionNode.getId());
        return Optional.of(new MaterialNodeWithAssets(questionNode, assets));
    }

    public List<com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto> getAllSpeakingSectionSummaries() {
        List<com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto> result = new ArrayList<>();
        // Find all SECTION nodes (root nodes for speaking sections)
        List<MaterialNode> sections = materialNodeRepository.findByKind("SECTION");
        for (MaterialNode section : sections) {
            // Find PART nodes under this section
            List<MaterialNode> parts = materialNodeRepository.findByParentNodeId(section.getId());
            Long part1Id = null;
            String part1Title = null;
            Long part2Id = null;
            String part2Title = null;
            for (MaterialNode part : parts) {
                if (part.getDisplayOrder() != null && part.getDisplayOrder() == 0) {
                    part1Id = part.getId();
                    part1Title = part.getTitle();
                } else if (part.getDisplayOrder() != null && part.getDisplayOrder() == 1) {
                    part2Id = part.getId();
                    part2Title = part.getTitle();
                }
            }
            result.add(com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto.builder()
                    .sectionId(section.getId())
                    .sectionTitle(section.getTitle())
                    .part1Id(part1Id)
                    .part1Title(part1Title)
                    .part2Id(part2Id)
                    .part2Title(part2Title)
                    .build());
        }
        return result;
    }

    public static class MaterialNodeWithAssets {
        public final MaterialNode node;
        public final List<MaterialAsset> assets;
        public MaterialNodeWithAssets(MaterialNode node, List<MaterialAsset> assets) {
            this.node = node;
            this.assets = assets;
        }
    }
}
