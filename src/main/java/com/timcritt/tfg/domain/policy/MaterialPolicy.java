package com.timcritt.tfg.domain.policy;

import com.timcritt.tfg.domain.model.Material;

public interface MaterialPolicy {
    void validateForPublication(Material material);
}