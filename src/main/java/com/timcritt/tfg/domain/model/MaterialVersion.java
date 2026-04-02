package com.timcritt.tfg.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialVersion {
    private Long id;
    private Long materialId;
    private Integer versionNo;
    private String status;
    private String changeSummary;
    private Long createdBy;
    private Instant publishedAt;
    private Map<String, Object> blueprintSnapshot;
    private Boolean isLocked;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

