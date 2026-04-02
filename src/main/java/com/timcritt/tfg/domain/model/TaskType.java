package com.timcritt.tfg.domain.model;

import java.time.Instant;

public class TaskType {
    private Long id;
    private String code;
    private String name;
    private Long examFamilyId;
    private Long skillId;
    private String description;
    private String configSchema; // JSON as String for domain
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getExamFamilyId() { return examFamilyId; }
    public void setExamFamilyId(Long examFamilyId) { this.examFamilyId = examFamilyId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getConfigSchema() { return configSchema; }
    public void setConfigSchema(String configSchema) { this.configSchema = configSchema; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

