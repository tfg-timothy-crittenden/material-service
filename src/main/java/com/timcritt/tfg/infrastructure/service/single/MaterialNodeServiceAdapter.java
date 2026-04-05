package com.timcritt.tfg.infrastructure.service.single;

import com.timcritt.tfg.application.service.single.MaterialNodeUseCaseService;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialNodeServiceAdapter {
    private final MaterialNodeUseCaseService delegate;

    public MaterialNodeServiceAdapter(MaterialNodeRepositoryPort repository) {
        this.delegate = new MaterialNodeUseCaseService(repository);
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
}
