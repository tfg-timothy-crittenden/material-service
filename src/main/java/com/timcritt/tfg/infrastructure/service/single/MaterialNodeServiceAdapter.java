package com.timcritt.tfg.infrastructure.service.single;

import com.timcritt.tfg.application.service.single.MaterialNodeUseCaseService;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.application.port.inbound.MaterialAssetUseCase;
import com.timcritt.tfg.domain.model.MaterialAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialNodeServiceAdapter {
    private final MaterialNodeUseCaseService delegate;
    private final MaterialAssetUseCase materialAssetUseCase;

    @Autowired
    public MaterialNodeServiceAdapter(MaterialNodeRepositoryPort repository, MaterialAssetUseCase materialAssetUseCase) {
        this.delegate = new MaterialNodeUseCaseService(repository);
        this.materialAssetUseCase = materialAssetUseCase;
    }

    @Transactional
    public MaterialNode create(MaterialNode node) {
        return delegate.create(node);
    }

    @Transactional(readOnly = true)
    public Optional<MaterialNode> findById(Long id) {
        return delegate.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MaterialNode> findAll() {
        return delegate.findAll();
    }

    @Transactional
    public MaterialNode update(MaterialNode node) {
        return delegate.update(node);
    }

    @Transactional
    public void delete(Long id) {
        delegate.delete(id);
    }

    @Transactional(readOnly = true)
    public List<MaterialNode> findByKind(String kind) {
        return delegate.findByKind(kind);
    }

    @Transactional(readOnly = true)
    public List<MaterialNode> findByKindAndExamFamilyIdAndSkillId(String kind, Long examFamilyId, Long skillId) {
        return delegate.findByKindAndExamFamilyIdAndSkillId(kind, examFamilyId, skillId);
    }

    @Transactional(readOnly = true)
    public List<MaterialNode> findByParentNodeId(Long parentNodeId) {
        return delegate.findByParentNodeId(parentNodeId);
    }

    @Transactional(readOnly = true)
    public Optional<MaterialNode> findByParentIdAndDisplayOrder(Long parentId, Integer displayOrder) {
        return delegate.findByParentIdAndDisplayOrder(parentId, displayOrder);
    }

    @Transactional(readOnly = true)
    public List<MaterialAsset> findMaterialAssetsByMaterialNodeId(Long materialNodeId) {
        return materialAssetUseCase.findByMaterialNodeId(materialNodeId);
    }
}
