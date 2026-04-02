package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.MaterialVersionRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialVersion;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialVersionJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialVersionJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class MaterialVersionRepositoryAdapter implements MaterialVersionRepositoryPort {
    private final MaterialVersionJpaRepository repository;
    public MaterialVersionRepositoryAdapter(MaterialVersionJpaRepository repository) {
        this.repository = repository;
    }
    @Override
    public MaterialVersion save(MaterialVersion materialVersion) {
        MaterialVersionJpaEntity entity = MaterialVersionEntityMapper.toEntity(materialVersion);
        MaterialVersionJpaEntity saved = repository.save(entity);
        return MaterialVersionEntityMapper.toDomain(saved);
    }
    @Override
    public Optional<MaterialVersion> findById(Long id) {
        return repository.findById(id).map(MaterialVersionEntityMapper::toDomain);
    }
    @Override
    public Boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}

