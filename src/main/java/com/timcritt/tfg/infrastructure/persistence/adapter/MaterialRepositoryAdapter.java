package com.timcritt.tfg.infrastructure.persistence.adapter;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.assembler.MaterialAggregateAssembler;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialAssetEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialNodeEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialNodeJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
public class MaterialRepositoryAdapter implements MaterialRepositoryPort {

    private final MaterialJpaRepository repository;
    private final MaterialNodeJpaRepository nodeRepository;
    private final MaterialAssetJpaRepository assetRepository;
    private final MaterialAggregateAssembler aggregateAssembler;

    public MaterialRepositoryAdapter(
            MaterialJpaRepository repository,
            MaterialNodeJpaRepository nodeRepository,
            MaterialAssetJpaRepository assetRepository,
            MaterialAggregateAssembler aggregateAssembler
    ) {
        this.repository = repository;
        this.nodeRepository = nodeRepository;
        this.assetRepository = assetRepository;
        this.aggregateAssembler = aggregateAssembler;
    }

    @Override
    @Transactional
    public Material save(Material material) {
        MaterialJpaEntity entity = MaterialEntityMapper.toEntity(material);
        MaterialJpaEntity saved = repository.save(entity);

        if (material != null && material.getRoot() != null) {
            List<MaterialNode> attachedNodes = flattenNodeTree(material.getRoot());

            if (containsTransientNodes(attachedNodes)) {
                List<MaterialNodeJpaEntity> savedNodeEntities = persistNodeTree(saved, material.getRoot());
                reconcileAttachedAssets(attachedNodes, savedNodeEntities);
            } else {
                List<MaterialNodeJpaEntity> nodeEntities = attachedNodes.stream()
                        .map(MaterialNodeEntityMapper::toEntity)
                        .toList();

                if (!nodeEntities.isEmpty()) {
                    List<MaterialNodeJpaEntity> savedNodeEntities = new ArrayList<>(nodeRepository.saveAll(nodeEntities));
                    reconcileAttachedAssets(attachedNodes, savedNodeEntities);
                }
            }
        }

        return assembleAggregate(saved.getId()).orElseThrow(() -> new IllegalStateException(
                "Saved material " + saved.getId() + " could not be reassembled"
        ));
    }

    @Override
    public Optional<Material> findById(Long id) {
        return assembleAggregate(id);
    }

    private Optional<Material> assembleAggregate(Long id) {
        return repository.findById(id)
                .map(materialEntity -> {

                    List<MaterialNodeJpaEntity> nodes =
                            nodeRepository.findByMaterialId(id);

                    List<Long> nodeIds = nodes.stream()
                            .map(MaterialNodeJpaEntity::getId)
                            .toList();

                    List<MaterialAssetEntity> assets =
                            nodeIds.isEmpty()
                                    ? List.of()
                                    : assetRepository.findByMaterialNode_IdIn(nodeIds);

                    return aggregateAssembler.assemble(
                            materialEntity,
                            nodes,
                            assets
                    );
                });
    }

    @Override
    public Boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    @Override
    public List<Material> findByExamFamilyId(Long examFamilyId) {
        return repository.findByExamFamilyId(examFamilyId)
                .stream()
                .map(MaterialEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Material> findAll() {
        return repository.findAll()
                .stream()
                .map(MaterialEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Material> findByMaterialNodeId(Long materialNodeId) {
        return repository.findByMaterialNodeId(materialNodeId)
                .map(MaterialEntityMapper::toDomain);
    }

    private boolean containsTransientNodes(List<MaterialNode> attachedNodes) {
        return attachedNodes.stream().anyMatch(node -> node.getId() == null);
    }

    private List<MaterialNodeJpaEntity> persistNodeTree(
            MaterialJpaEntity savedMaterial,
            MaterialNode root
    ) {
        IdentityHashMap<MaterialNode, MaterialNodeJpaEntity> savedNodesByDomainInstance = new IdentityHashMap<>();
        MaterialNodeJpaEntity savedRoot = persistNodeRecursively(
                root,
                savedMaterial.getId(),
                null,
                savedNodesByDomainInstance
        );

        if (!Objects.equals(savedMaterial.getMaterialNodeId(), savedRoot.getId())) {
            savedMaterial.setMaterialNodeId(savedRoot.getId());
            repository.save(savedMaterial);
        }

        return new ArrayList<>(savedNodesByDomainInstance.values());
    }

    private MaterialNodeJpaEntity persistNodeRecursively(
            MaterialNode domainNode,
            Long persistedMaterialId,
            Long persistedParentNodeId,
            IdentityHashMap<MaterialNode, MaterialNodeJpaEntity> savedNodesByDomainInstance
    ) {
        MaterialNodeJpaEntity savedNode = nodeRepository.save(
                MaterialNodeEntityMapper.toEntity(
                        domainNode,
                        persistedMaterialId,
                        persistedParentNodeId
                )
        );

        savedNodesByDomainInstance.put(domainNode, savedNode);

        for (MaterialNode child : domainNode.getChildren()) {
            persistNodeRecursively(
                    child,
                    persistedMaterialId,
                    savedNode.getId(),
                    savedNodesByDomainInstance
            );
        }

        return savedNode;
    }

    private void reconcileAttachedAssets(List<MaterialNode> attachedNodes, List<MaterialNodeJpaEntity> savedNodeEntities) {
        List<Long> attachedNodeIds = savedNodeEntities.stream()
                .map(MaterialNodeJpaEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        List<MaterialAssetEntity> persistedAssets = attachedNodeIds.isEmpty()
                ? List.of()
                : assetRepository.findByMaterialNode_IdIn(attachedNodeIds);

        Map<Long, MaterialNodeJpaEntity> nodeEntitiesById = new HashMap<>();
        for (MaterialNodeJpaEntity nodeEntity : savedNodeEntities) {
            if (nodeEntity.getId() != null) {
                nodeEntitiesById.put(nodeEntity.getId(), nodeEntity);
            }
        }

        List<MaterialAssetEntity> assetEntitiesToSave = new ArrayList<>();
        Set<Long> attachedAssetIds = new HashSet<>();
        for (MaterialNode node : attachedNodes) {
            MaterialNodeJpaEntity owningNodeEntity = nodeEntitiesById.get(node.getId());
            if (owningNodeEntity == null && !node.getAssets().isEmpty()) {
                throw new IllegalStateException("Owning node " + node.getId() + " not found while saving attached assets");
            }

            for (MaterialAsset asset : node.getAssets()) {
                if (asset.getId() != null) {
                    attachedAssetIds.add(asset.getId());
                }
                assetEntitiesToSave.add(MaterialAssetEntityMapper.toEntity(asset, owningNodeEntity));
            }
        }

        if (!assetEntitiesToSave.isEmpty()) {
            assetRepository.saveAll(assetEntitiesToSave);
        }

        Set<Long> persistedAssetIdsToDelete = persistedAssets.stream()
                .map(MaterialAssetEntity::getId)
                .filter(Objects::nonNull)
                .filter(assetId -> !attachedAssetIds.contains(assetId))
                .collect(java.util.stream.Collectors.toSet());

        if (!persistedAssetIdsToDelete.isEmpty()) {
            assetRepository.deleteAllById(persistedAssetIdsToDelete);
        }
    }

    private List<MaterialNode> flattenNodeTree(MaterialNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        List<MaterialNode> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private void collectNodes(MaterialNode current, List<MaterialNode> nodes) {
        nodes.add(current);
        for (MaterialNode child : current.getChildren()) {
            collectNodes(child, nodes);
        }
    }
}
