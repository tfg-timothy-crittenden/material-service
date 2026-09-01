package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.service.materialdetails.MaterialDetailsRequestService;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "spring.kafka.listener", name = "auto-startup", havingValue = "true", matchIfMissing = true)
public class MaterialDetailsRequestedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialDetailsRequestedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final MaterialDetailsRequestService requestService;
    private final MessagingKafkaProperties messagingKafkaProperties;

    public MaterialDetailsRequestedKafkaListener(
            ObjectMapper objectMapper,
            MaterialDetailsRequestService requestService,
            MessagingKafkaProperties messagingKafkaProperties) {
        this.objectMapper = objectMapper;
        this.requestService = requestService;
        this.messagingKafkaProperties = messagingKafkaProperties;
    }

    @KafkaListener(
            topics = "${material.kafka.material-details-requested-topic:material.details.requested.v1}",
            groupId = "${material.kafka.material-details-requested-group-id:material-service-material-details-requested}")
    public void onMessage(String payload) {
        if (!messagingKafkaProperties.isEnabled()) {
            log.debug("Ignoring material details request because messaging.kafka.enabled is false");
            return;
        }

        if (payload == null || payload.trim().isEmpty()) {
            log.warn("Ignoring empty material details request message");
            return;
        }

        MaterialDetailsRequestedPayload request;
        try {
            request = objectMapper.readValue(payload, MaterialDetailsRequestedPayload.class);
        } catch (JsonProcessingException ex) {
            log.warn("Ignoring malformed material details request message", ex);
            return;
        }

        log.info("Received material details request: requestId={}, materialIdsCount={}, materialIds={}",
                safeRequestId(request),
                request == null || request.getMaterialIds() == null ? null : request.getMaterialIds().size(),
                formatMaterialIds(request == null ? null : request.getMaterialIds()));

        String invalidReason = validate(request);
        if (invalidReason != null) {
            log.warn("Ignoring invalid material details request message: reason={}, requestId={}, materialIdsCount={}, materialIds={}",
                    invalidReason,
                    safeRequestId(request),
                    request == null || request.getMaterialIds() == null ? null : request.getMaterialIds().size(),
                    formatMaterialIds(request == null ? null : request.getMaterialIds()));
            return;
        }

        requestService.processRequest(request);
    }

    private String validate(MaterialDetailsRequestedPayload request) {
        if (request == null) {
            return "request is null";
        }
        if (!hasText(request.getRequestId())) {
            return "requestId is missing or blank";
        }
        if (request.getMaterialIds() == null || request.getMaterialIds().isEmpty()) {
            return "materialIds is empty";
        }
        if (request.getRequestedAt() == null) {
            return "requestedAt is missing";
        }

        if (request.getMaterialIds().stream().anyMatch(Objects::isNull)) {
            return "materialIds contains null values";
        }

        for (Long materialId : request.getMaterialIds()) {
            if (materialId == null || materialId <= 0) {
                return "materialIds must contain only positive ids";
            }
        }

        if (new LinkedHashSet<>(request.getMaterialIds()).size() != request.getMaterialIds().size()) {
            return "materialIds must be unique";
        }

        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeRequestId(MaterialDetailsRequestedPayload request) {
        return request == null ? null : trimToNull(request.getRequestId());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatMaterialIds(List<Long> materialIds) {
        if (materialIds == null) {
            return null;
        }
        if (materialIds.size() <= 10) {
            return materialIds.toString();
        }
        return materialIds.subList(0, 10) + " ... (total=" + materialIds.size() + ")";
    }
}

