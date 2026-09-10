package com.timcritt.tfg.infrastructure.persistence.adapter;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.infrastructure.persistence.assembler.MaterialAggregateAssembler;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
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
import java.util.Optional;

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
            List<MaterialNodeJpaEntity> nodeEntities = flattenNodeTree(material.getRoot()).stream()
                    .map(MaterialNodeEntityMapper::toEntity)
                    .toList();

            if (!nodeEntities.isEmpty()) {
                nodeRepository.saveAll(nodeEntities);
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

    private List<com.timcritt.tfg.domain.model.MaterialNode> flattenNodeTree(com.timcritt.tfg.domain.model.MaterialNode root) {
        if (root == null) {
            return Collections.emptyList();
        }

        List<com.timcritt.tfg.domain.model.MaterialNode> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private void collectNodes(com.timcritt.tfg.domain.model.MaterialNode current, List<com.timcritt.tfg.domain.model.MaterialNode> nodes) {
        nodes.add(current);
        for (com.timcritt.tfg.domain.model.MaterialNode child : current.getChildren()) {
            collectNodes(child, nodes);
        }
    }
}
