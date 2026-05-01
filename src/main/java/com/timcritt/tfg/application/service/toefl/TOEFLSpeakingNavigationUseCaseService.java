package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.MaterialNodeWithAssetsResult;
import com.timcritt.tfg.application.dto.SpeakingQuestionEditResult;
import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.dto.SpeakingSectionSummary;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TOEFLSpeakingNavigationUseCaseService implements TOEFLSpeakingNavigationUseCase {
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

    @Override
    public Optional<MaterialNodeWithAssetsResult> getQuestion(Long materialId, int partOrder, int questionOrder) {
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
        return Optional.of(new MaterialNodeWithAssetsResult(questionNode, assets));
    }

    @Override
    public Optional<SpeakingSectionEditResult> getSpeakingSectionForEdit(Long materialId) {
        Optional<Material> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isEmpty() || materialOpt.get().getMaterialNodeId() == null) {
            return Optional.empty();
        }

        Material material = materialOpt.get();
        Optional<MaterialNode> rootOpt = materialNodeRepository.findById(material.getMaterialNodeId());
        if (rootOpt.isEmpty()) {
            return Optional.empty();
        }

        MaterialNode rootNode = rootOpt.get();
        Optional<MaterialNode> part1Opt = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 0);
        Optional<MaterialNode> part2Opt = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 1);

        return Optional.of(SpeakingSectionEditResult.builder()
                .materialId(material.getId())
                .sectionId(rootNode.getId())
                .materialTitle(material.getTitle())
                .materialDescription(material.getDescription())
                .partTitle(part1Opt.map(MaterialNode::getTitle).orElse(null))
                .partImageStorageKey(part1Opt.map(this::findImageStorageKey).orElse(null))
                .questions(part1Opt.map(this::toQuestionEditList).orElse(List.of()))
                .part2Title(part2Opt.map(MaterialNode::getTitle).orElse(null))
                .part2Questions(part2Opt.map(this::toQuestionEditList).orElse(List.of()))
                .build());
    }

    @Override
    public List<SpeakingSectionSummary> getAllSpeakingSectionSummaries() {
        return getSpeakingSectionSummariesByStatus(MaterialStatus.PUBLISHED);
    }

    @Override
    public List<SpeakingSectionSummary> getDraftSpeakingSectionSummaries() {
        return getSpeakingSectionSummariesByStatus(MaterialStatus.DRAFT);
    }

    private List<SpeakingSectionSummary> getSpeakingSectionSummariesByStatus(MaterialStatus status) {
        List<SpeakingSectionSummary> result = new ArrayList<>();
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
            Material mat = materialRepository.findByMaterialNodeId(section.getId()).orElse(null);
            if (mat == null || mat.getStatus() != status) {
                continue;
            }
            result.add(SpeakingSectionSummary.builder()
                    .materialId(mat.getId())
                    .sectionId(section.getId())
                    .sectionTitle(section.getTitle())
                    .part1Id(part1Id)
                    .part1Title(part1Title)
                    .part2Id(part2Id)
                    .part2Title(part2Title)
                    .status(mat.getStatus())
                    .createdAt(mat.getCreatedAt())
                    .updatedAt(mat.getUpdatedAt())
                    .build());
        }
        return result;
    }

    private String findImageStorageKey(MaterialNode partNode) {
        return materialAssetRepository.findByMaterialNodeId(partNode.getId()).stream()
                .filter(a -> a.getKind() == MaterialAsset.Kind.IMAGE)
                .sorted(Comparator.comparing(MaterialAsset::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(MaterialAsset::getStorageKey)
                .findFirst()
                .orElse(null);
    }

    private List<SpeakingQuestionEditResult> toQuestionEditList(MaterialNode partNode) {
        return materialNodeRepository.findByParentNodeId(partNode.getId()).stream()
                // Question nodes are currently persisted as ITEM; keep QUESTION for backward compatibility.
                .filter(node -> "ITEM".equalsIgnoreCase(node.getKind()) || "QUESTION".equalsIgnoreCase(node.getKind()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(node -> SpeakingQuestionEditResult.builder()
                        .index(node.getDisplayOrder())
                        .questionNodeId(node.getId())
                        .transcriptText(node.getTranscriptText())
                        .config(node.getConfig())
                        .audioStorageKey(findAudioStorageKey(node.getId()))
                        .build())
                .toList();
    }

    @Override
    public List<MaterialAsset> getAssetsByMaterialNodeId(Long nodeId) {
        return materialAssetRepository.findByMaterialNodeId(nodeId);
    }

    private String findAudioStorageKey(Long questionNodeId) {
        return materialAssetRepository.findByMaterialNodeId(questionNodeId).stream()
                .filter(a -> a.getKind() == MaterialAsset.Kind.AUDIO)
                .sorted(Comparator.comparing(MaterialAsset::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(MaterialAsset::getStorageKey)
                .findFirst()
                .orElse(null);
    }
}
