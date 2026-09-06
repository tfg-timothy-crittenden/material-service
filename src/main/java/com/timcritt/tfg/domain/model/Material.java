package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.policy.MaterialPolicy;

import java.time.Instant;
import java.util.Objects;

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
        if (title != null) {
            if (title.isBlank()) {
                throw new IllegalArgumentException(
                        "title cannot be blank"
                );
            }

            this.title = title.trim();
        }

        if (description != null) {
            this.description = description.trim();
        }

        incrementVersion();
        touch();
    }

    // ***************************** INTERNAL DOMAIN HELPERS *****************************

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