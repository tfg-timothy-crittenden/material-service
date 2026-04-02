package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.MaterialNode;
import java.util.Optional;
import java.util.List;

public interface MaterialNodeRepositoryPort {
    MaterialNode save(MaterialNode materialNode);
    Optional<MaterialNode> findById(Long id);
    List<MaterialNode> findAll();
    void deleteById(Long id);
}

