package com.timcritt.tfg.infrastructure.service;
import com.timcritt.tfg.application.port.inbound.MaterialUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.service.MaterialUseCaseService;
import com.timcritt.tfg.domain.model.Material;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class MaterialServiceAdapter implements MaterialUseCase {
    private final MaterialUseCaseService delegate;
    public MaterialServiceAdapter(MaterialRepositoryPort repository) {
        this.delegate = new MaterialUseCaseService(repository);
    }
    @Override
    @Transactional
    public Material createMaterial(Material material) {
        return delegate.createMaterial(material);
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<Material> findMaterialById(Long id) {
        return delegate.findMaterialById(id);
    }
    @Override
    @Transactional
    public Material updateMaterial(Material material) {
        return delegate.updateMaterial(material);
    }
    @Override
    @Transactional
    public Boolean deleteMaterial(Long id) {
        return delegate.deleteMaterial(id);
    }
}

