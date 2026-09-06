package com.timcritt.tfg.infrastructure.persistence.adapter;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.infrastructure.persistence.assembler.MaterialAggregateAssembler;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialNodeJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
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
    public Material save(Material material) {
        MaterialJpaEntity entity = MaterialEntityMapper.toEntity(material);
        MaterialJpaEntity saved = repository.save(entity);

        return MaterialEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Material> findById(Long id) {
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
}
