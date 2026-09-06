package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.policy.MaterialPolicy;

import java.time.Instant;
import java.util.Objects;

public class Material {

    private Long id;
    private Long examFamilyId;

    private Long materialNodeId;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamFamilyId() {
        return examFamilyId;
    }

    public void setExamFamilyId(Long examFamilyId) {
        this.examFamilyId = examFamilyId;
    }

    public Long getMaterialNodeId() {
        return materialNodeId;
    }

    public void setMaterialNodeId(Long materialNodeId) {
        this.materialNodeId = materialNodeId;
    }

    public MaterialNode getRoot() {
        return root;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getOwnerOrgId() {
        return ownerOrgId;
    }

    public void setOwnerOrgId(Long ownerOrgId) {
        this.ownerOrgId = ownerOrgId;
    }

    public MaterialStatus getStatus() {
        return status;
    }

    public void setStatus(MaterialStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

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
        this.materialNodeId = root.getId();
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
        this.version = version == null ? 1L : version + 1;
        this.updatedAt = Instant.now();
    }

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

        public Builder materialNodeId(Long materialNodeId) {
            material.materialNodeId = materialNodeId;
            return this;
        }

        public Builder root(MaterialNode root) {
            material.root = root;
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