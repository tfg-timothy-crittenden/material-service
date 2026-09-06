package com.timcritt.tfg.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialDetailsRequestedPayload {
    private String requestId;
    private List<Long> materialIds;
    private Instant requestedAt;

    public MaterialDetailsRequestedPayload() {
    }

    public MaterialDetailsRequestedPayload(String requestId, List<Long> materialIds, Instant requestedAt) {
        this.requestId = requestId;
        this.materialIds = materialIds;
        this.requestedAt = requestedAt;
    }

    public static Builder builder() { return new Builder(); }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public List<Long> getMaterialIds() { return materialIds; }
    public void setMaterialIds(List<Long> materialIds) { this.materialIds = materialIds; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public static class Builder {
        private String requestId;
        private List<Long> materialIds;
        private Instant requestedAt;

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder materialIds(List<Long> materialIds) { this.materialIds = materialIds; return this; }
        public Builder requestedAt(Instant requestedAt) { this.requestedAt = requestedAt; return this; }

        public MaterialDetailsRequestedPayload build() {
            return new MaterialDetailsRequestedPayload(requestId, materialIds, requestedAt);
        }
    }
}

