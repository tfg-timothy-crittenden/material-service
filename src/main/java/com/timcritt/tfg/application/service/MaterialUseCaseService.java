package com.timcritt.tfg.application.service;
import com.timcritt.tfg.application.port.inbound.MaterialUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import java.util.Optional;

public class MaterialUseCaseService implements MaterialUseCase {
    private final MaterialRepositoryPort repository;
    public MaterialUseCaseService(MaterialRepositoryPort repository) {
        this.repository = repository;
    }
    @Override
    public Material createMaterial(Material material) {
        return repository.save(material);
    }
    @Override
    public Optional<Material> findMaterialById(Long id) {
        return repository.findById(id);
    }
    @Override
    public Material updateMaterial(Material material) {
        return repository.save(material);
    }
    @Override
    public Boolean deleteMaterial(Long id) {
        return repository.delete(id);
    }
}

