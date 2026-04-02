package com.timcritt.tfg.infrastructure.persistence.adapter;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class MaterialRepositoryAdapter implements MaterialRepositoryPort {
    private final MaterialJpaRepository repository;
    public MaterialRepositoryAdapter(MaterialJpaRepository repository) {
        this.repository = repository;
    }
    @Override
    public Material save(Material material) {
        MaterialJpaEntity entity = MaterialEntityMapper.toEntity(material);
        MaterialJpaEntity saved = repository.save(entity);
        return MaterialEntityMapper.toDomain(saved);
    }
    @Override
    public Optional<Material> findById(Long id) {
        return repository.findById(id).map(MaterialEntityMapper::toDomain);
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

