package com.timcritt.tfg.domain.model;

public record AssetChange(
        String previousStorageKey,
        String currentStorageKey
) {
}

