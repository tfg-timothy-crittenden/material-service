package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.policy.MaterialPolicy;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Material {

    private Long id;
    private Long examFamilyId;

    private MaterialNode root;

    private String title;
    private String description;
    private Long authorId;
    private Long ownerOrgId;

    private MaterialStatus status;

    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    private Material() {
    }

    // ***************************** GETTERS *****************************

    public Long getId() {
        return id;
    }

    public Long getExamFamilyId() {
        return examFamilyId;
    }

    public MaterialNode getRoot() {
        return root;
    }

    public boolean hasRoot() {
        return root != null;
    }

    public Long getRootId() {
        return root != null ? root.getId() : null;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Long getOwnerOrgId() {
        return ownerOrgId;
    }

    public MaterialStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ***************************** DOMAIN BEHAVIOUR *****************************

    public void attachRoot(MaterialNode root) {
        Objects.requireNonNull(root, "root cannot be null");

        if (root.getParentNodeId() != null) {
            throw new IllegalArgumentException(
                    "Material root cannot have a parent"
            );
        }

        if (!Objects.equals(id, root.getMaterialId())) {
            throw new IllegalArgumentException(
                    "Root node must belong to this material"
            );
        }

        this.root = root;
    }

    public void publish(MaterialPolicy policy) {
        Objects.requireNonNull(policy, "policy cannot be null");

        if (status == MaterialStatus.PUBLISHED) {
            return;
        }

        if (status != MaterialStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft materials can be published"
            );
        }

        policy.validateForPublication(this);

        this.status = MaterialStatus.PUBLISHED;
        incrementVersion();
        touch();
    }

    public void updateDetails(
            String title,
            String description
    ) {
        String normalizedTitle = title == null ? null : title.trim();
        if (normalizedTitle != null && normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("title cannot be blank");
        }
        String normalizedDescription = description == null ? null : description.trim();

        boolean changed = false;
        if (normalizedTitle != null) {
            if (!Objects.equals(this.title, normalizedTitle)) {
                this.title = normalizedTitle;
                changed = true;
            }
            if (root != null) {
                Long previousRootVersion = root.getVersion();
                root.updateTitle(normalizedTitle);
                changed |= !Objects.equals(previousRootVersion, root.getVersion());
            }
        }

        if (normalizedDescription != null && !Objects.equals(this.description, normalizedDescription)) {
            this.description = normalizedDescription;
            changed = true;
        }

        if (changed) {
            incrementVersion();
            touch();
        }
    }

    public void updateNodeTitle(Long nodeId, String title) {
        MaterialNode node = requireAttachedNode(nodeId);
        Long previousVersion = node.getVersion();
        node.updateTitle(title);

        if (!Objects.equals(previousVersion, node.getVersion())) {
            incrementVersion();
            touch();
        }
    }

    public void updateNodeTranscript(Long nodeId, String transcriptText) {
        MaterialNode node = requireAttachedNode(nodeId);
        Long previousVersion = node.getVersion();
        node.updateTranscriptText(transcriptText);

        if (!Objects.equals(previousVersion, node.getVersion())) {
            incrementVersion();
            touch();
        }
    }

    public void updateNodeConfig(Long nodeId, Map<String, Object> config) {
        MaterialNode node = requireAttachedNode(nodeId);
        Long previousVersion = node.getVersion();
        node.updateConfig(config);

        if (!Objects.equals(previousVersion, node.getVersion())) {
            incrementVersion();
            touch();
        }
    }

    public AssetChange replaceNodeAssetFile(
            Long nodeId,
            Long assetId,
            String storageKey,
            String originalFilename,
            String mimeType,
            Long fileSizeBytes
    ) {
        MaterialNode node = requireAttachedNode(nodeId);
        MaterialAsset asset = requireAttachedAsset(node, assetId);
        String previousStorageKey = asset.getStorageKey();

        asset.replaceFile(storageKey, originalFilename, mimeType, fileSizeBytes);

        incrementVersion();
        touch();
        return new AssetChange(previousStorageKey, asset.getStorageKey());
    }

    public AssetChange removeNodeAsset(Long nodeId, Long assetId) {
        MaterialNode node = requireAttachedNode(nodeId);
        MaterialAsset removedAsset = node.removeAssetById(assetId);

        incrementVersion();
        touch();
        return new AssetChange(removedAsset.getStorageKey(), null);
    }

    public AssetChange addNodeAsset(
            Long nodeId,
            MaterialAsset.Kind kind,
            String storageKey,
            String originalFilename,
            String mimeType,
            Long fileSizeBytes
    ) {
        MaterialNode node = requireAttachedNode(nodeId);
        MaterialAsset asset = MaterialAsset.create(
                node.getId(),
                kind,
                storageKey,
                originalFilename,
                mimeType,
                fileSizeBytes
        );

        node.addAsset(asset);
        incrementVersion();
        touch();
        return new AssetChange(null, asset.getStorageKey());
    }

    // ***************************** INTERNAL DOMAIN HELPERS *****************************

    private MaterialNode requireAttachedNode(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId cannot be null");
        }

        ArrayDeque<MaterialNode> toVisit = new ArrayDeque<>();
        Set<MaterialNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        if (root != null) {
            toVisit.add(root);
        }

        while (!toVisit.isEmpty()) {
            MaterialNode node = toVisit.removeFirst();
            if (!visited.add(node)) {
                continue;
            }
            if (Objects.equals(nodeId, node.getId())) {
                return node;
            }
            toVisit.addAll(node.getChildren());
        }

        throw new IllegalArgumentException("No attached node with ID " + nodeId + " for material " + id);
    }

    private MaterialAsset requireAttachedAsset(MaterialNode node, Long assetId) {
        if (assetId == null) {
            throw new IllegalArgumentException("assetId cannot be null");
        }

        for (MaterialAsset asset : node.getAssets()) {
            if (Objects.equals(assetId, asset.getId())) {
                return asset;
            }
        }

        throw new IllegalArgumentException(
                "No attached asset with ID " + assetId + " under node " + node.getId() + " for material " + id
        );
    }

    private void incrementVersion() {
        this.version = version == null ? 1L : version + 1;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    // ***************************** REHYDRATION BUILDER *****************************

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Material material = new Material();

        public Builder id(Long id) {
            material.id = id;
            return this;
        }

        public Builder examFamilyId(Long examFamilyId) {
            material.examFamilyId = examFamilyId;
            return this;
        }

        public Builder title(String title) {
            material.title = title;
            return this;
        }

        public Builder description(String description) {
            material.description = description;
            return this;
        }

        public Builder authorId(Long authorId) {
            material.authorId = authorId;
            return this;
        }

        public Builder ownerOrgId(Long ownerOrgId) {
            material.ownerOrgId = ownerOrgId;
            return this;
        }

        public Builder status(MaterialStatus status) {
            material.status = status;
            return this;
        }

        public Builder version(Long version) {
            material.version = version;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            material.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            material.updatedAt = updatedAt;
            return this;
        }

        public Material build() {
            return material;
        }
    }
}