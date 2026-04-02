package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.MaterialAsset;
import java.time.OffsetDateTime;
import java.util.Map;

public class MaterialAssetDto {
    public Long id;
    public Long materialNodeId;
    public String kind;
    public String storageKey;
    public String originalFilename;
    public String mimeType;
    public Long fileSizeBytes;
    public String title;
    public String transcriptText;
    public Integer displayOrder;
    public Map<String, Object> metadata;
    public Long version;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    public static MaterialAssetDto fromDomain(MaterialAsset asset) {
        MaterialAssetDto dto = new MaterialAssetDto();
        dto.id = asset.getId();
        dto.materialNodeId = asset.getMaterialNodeId();
        dto.kind = asset.getKind().name();
        dto.storageKey = asset.getStorageKey();
        dto.originalFilename = asset.getOriginalFilename();
        dto.mimeType = asset.getMimeType();
        dto.fileSizeBytes = asset.getFileSizeBytes();
        dto.title = asset.getTitle();
        dto.transcriptText = asset.getTranscriptText();
        dto.displayOrder = asset.getDisplayOrder();
        dto.metadata = asset.getMetadata();
        dto.version = asset.getVersion();
        dto.createdAt = asset.getCreatedAt();
        dto.updatedAt = asset.getUpdatedAt();
        return dto;
    }

    public MaterialAsset toDomain() {
        MaterialAsset asset = new MaterialAsset();
        asset.setId(id);
        asset.setMaterialNodeId(materialNodeId);
        asset.setKind(MaterialAsset.Kind.valueOf(kind));
        asset.setStorageKey(storageKey);
        asset.setOriginalFilename(originalFilename);
        asset.setMimeType(mimeType);
        asset.setFileSizeBytes(fileSizeBytes);
        asset.setTitle(title);
        asset.setTranscriptText(transcriptText);
        asset.setDisplayOrder(displayOrder);
        asset.setMetadata(metadata);
        asset.setVersion(version);
        asset.setCreatedAt(createdAt);
        asset.setUpdatedAt(updatedAt);
        return asset;
    }
}

