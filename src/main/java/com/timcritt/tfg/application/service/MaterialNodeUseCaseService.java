package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialNode;
import java.util.List;
import java.util.Optional;

public class MaterialNodeUseCaseService {
    private final MaterialNodeRepositoryPort repository;

    public MaterialNodeUseCaseService(MaterialNodeRepositoryPort repository) {
        this.repository = repository;
    }

    public MaterialNode create(MaterialNode node) {
        return repository.save(node);
    }

    public Optional<MaterialNode> findById(Long id) {
        return repository.findById(id);
    }

    public List<MaterialNode> findAll() {
        return repository.findAll();
    }

    public MaterialNode update(MaterialNode node) {
        return repository.save(node);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

