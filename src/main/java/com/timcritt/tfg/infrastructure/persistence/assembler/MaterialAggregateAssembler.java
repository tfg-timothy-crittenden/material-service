package com.timcritt.tfg.infrastructure.persistence.assembler;

import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialAssetEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialNodeEntityMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class MaterialAggregateAssembler {

    public Material assemble(
            MaterialJpaEntity materialEntity,
            List<MaterialNodeJpaEntity> nodeEntities,
            List<MaterialAssetEntity> assetEntities
    ) {
        Material material = MaterialEntityMapper.toDomain(materialEntity);

        if (materialEntity.getMaterialNodeId() == null) {
            return material;
        }

        MaterialNode root = assembleRoot(
                materialEntity.getMaterialNodeId(),
                nodeEntities,
                assetEntities
        );

        material.attachRoot(root);

        return material;
    }

    public MaterialNode assembleRoot(
            Long rootNodeId,
            List<MaterialNodeJpaEntity> nodeEntities,
            List<MaterialAssetEntity> assetEntities
    ) {

        Map<Long, MaterialNode> nodesById = new HashMap<>();

        // 1. Create all detached domain nodes
        for (MaterialNodeJpaEntity entity : nodeEntities) {
            MaterialNode node = MaterialNodeEntityMapper.toDomain(entity);
            nodesById.put(node.getId(), node);
        }

        // 2. Connect parent -> child relationships in displayOrder order per parent
        Map<Long, List<MaterialNode>> childrenByParent = new HashMap<>();
        for (MaterialNode node : nodesById.values()) {
            if (node.getParentNodeId() != null) {
                childrenByParent
                        .computeIfAbsent(node.getParentNodeId(), ignored -> new ArrayList<>())
                        .add(node);
            }
        }

        Comparator<MaterialNode> childOrder = Comparator
                .comparing(MaterialNode::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MaterialNode::getId, Comparator.nullsLast(Long::compareTo));

        for (Map.Entry<Long, List<MaterialNode>> entry : childrenByParent.entrySet()) {
            MaterialNode parent = nodesById.get(entry.getKey());

            if (parent == null) {
                throw new IllegalStateException(
                        "Parent node " + entry.getKey()
                                + " not found while assembling material aggregate"
                );
            }

            entry.getValue().stream()
                    .sorted(childOrder)
                    .forEach(parent::addChild);
        }

        // 3. Attach assets
        for (MaterialAssetEntity entity : assetEntities) {
            MaterialAsset asset =
                    MaterialAssetEntityMapper.toDomain(entity);

            MaterialNode owner =
                    nodesById.get(asset.getMaterialNodeId());

            if (owner == null) {
                throw new IllegalStateException(
                        "Node " + asset.getMaterialNodeId()
                                + " not found for asset " + asset.getId()
                );
            }

            owner.addAsset(asset);
        }

        // 4. Return the root
        MaterialNode root = nodesById.get(rootNodeId);

        if (root == null) {
            throw new IllegalStateException(
                    "Root node " + rootNodeId + " not found"
            );
        }

        if (root.getParentNodeId() != null) {
            throw new IllegalStateException(
                    "Configured root node has a parent: " + rootNodeId
            );
        }

        return root;
    }


}