package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialAsset;
import org.springframework.stereotype.Service;

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

    public static class MaterialNodeWithAssets {
        public final MaterialNode node;
        public final List<MaterialAsset> assets;
        public MaterialNodeWithAssets(MaterialNode node, List<MaterialAsset> assets) {
            this.node = node;
            this.assets = assets;
        }
    }
}
