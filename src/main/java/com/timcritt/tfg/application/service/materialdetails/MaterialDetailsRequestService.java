package com.timcritt.tfg.application.service.materialdetails;

import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.port.outbound.MaterialDetailsUpsertedEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.domain.model.Material;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MaterialDetailsRequestService {

    private final MaterialRepositoryPort materialRepository;
    private final TOEFLSpeakingNavigationUseCase navigationUseCase;
    private final MaterialDetailsUpsertedEventPublisherPort eventPublisher;

    public MaterialDetailsRequestService(
            MaterialRepositoryPort materialRepository,
            TOEFLSpeakingNavigationUseCase navigationUseCase,
            MaterialDetailsUpsertedEventPublisherPort eventPublisher) {
        this.materialRepository = materialRepository;
        this.navigationUseCase = navigationUseCase;
        this.eventPublisher = eventPublisher;
    }

    public void processRequest(MaterialDetailsRequestedPayload request) {
        if (request == null || request.getMaterialIds() == null || request.getMaterialIds().isEmpty()) {
            return;
        }

        String requestId = request.getRequestId() == null ? null : request.getRequestId().trim();
        if (requestId == null || requestId.isEmpty()) {
            return;
        }

        log.debug("Processing material details request: requestId={}, materialCount={}",
                requestId,
                request.getMaterialIds().size());

        for (Long materialId : uniqueMaterialIds(request.getMaterialIds())) {
            if (materialId == null || materialId <= 0) {
                log.warn("Skipping invalid material id in details request: requestId={}, materialId={}", requestId, materialId);
                continue;
            }

            Optional<Material> materialOpt = materialRepository.findById(materialId);
            if (materialOpt.isEmpty()) {
                log.warn("Skipping missing material in details request: requestId={}, materialId={}", requestId, materialId);
                continue;
            }

            Optional<SpeakingSectionEditResult> detailsOpt = navigationUseCase.getSpeakingSectionForEdit(materialId);

            Material material = materialOpt.get();
            if (detailsOpt.isEmpty()) {
                log.warn("Publishing fallback material details for material without speaking edit view: requestId={}, materialId={}", requestId, materialId);
            }
            SpeakingSectionEditResult details = detailsOpt.orElse(null);
            eventPublisher.publishMaterialDetailsUpserted(
                    MaterialDetailsUpsertedEvent.builder()
                            .materialId(material.getId())
                            .version(resolveVersion(material))
                            .materialTitle(resolveMaterialTitle(material, details))
                            .part1Title(details == null ? null : details.getPartTitle())
                            .part2Title(details == null ? null : details.getPart2Title())
                            .description(resolveDescription(material, details))
                            .updatedAt(resolveUpdatedAt(material))
                            .build(),
                    requestId);
        }
    }

    private List<Long> uniqueMaterialIds(List<Long> materialIds) {
        return new java.util.ArrayList<>(new LinkedHashSet<>(materialIds));
    }

    private Long resolveVersion(Material material) {
        if (material.getVersion() == null) {
            return 0L;
        }
        return Math.max(0L, material.getVersion());
    }

    private String resolveMaterialTitle(Material material, SpeakingSectionEditResult details) {
        if (details != null && details.getMaterialTitle() != null) {
            return details.getMaterialTitle();
        }
        return material.getTitle();
    }

    private String resolveDescription(Material material, SpeakingSectionEditResult details) {
        if (details != null) {
            return details.getMaterialDescription();
        }
        return material.getDescription();
    }

    private Instant resolveUpdatedAt(Material material) {
        return Optional.ofNullable(material.getUpdatedAt())
                .orElseGet(() -> Optional.ofNullable(material.getCreatedAt()).orElseGet(Instant::now));
    }
}

