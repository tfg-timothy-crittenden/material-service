package com.timcritt.tfg.application.service.materialdetails;

import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.integration.MaterialDetailsUpsertedOutboxMessage;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import com.timcritt.tfg.domain.model.Material;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;

public class MaterialDetailsRequestService {

    private final MaterialRepositoryPort materialRepository;
    private final TOEFLSpeakingNavigationUseCase navigationUseCase;
    private final IntegrationEventOutboxPort outboxPort;

    public MaterialDetailsRequestService(
            MaterialRepositoryPort materialRepository,
            TOEFLSpeakingNavigationUseCase navigationUseCase,
            IntegrationEventOutboxPort outboxPort) {
        this.materialRepository = materialRepository;
        this.navigationUseCase = navigationUseCase;
        this.outboxPort = outboxPort;
    }

    public void processRequest(MaterialDetailsRequestedPayload request) {
        if (request == null || request.getMaterialIds() == null || request.getMaterialIds().isEmpty()) {
            return;
        }

        String requestId = request.getRequestId() == null ? null : request.getRequestId().trim();
        if (requestId == null || requestId.isEmpty()) {
            return;
        }

        for (Long materialId : uniqueMaterialIds(request.getMaterialIds())) {
            if (materialId == null || materialId <= 0) {
                continue;
            }

            Optional<Material> materialOpt = materialRepository.findById(materialId);
            if (materialOpt.isEmpty()) {
                continue;
            }

            Optional<SpeakingSectionEditResult> detailsOpt = navigationUseCase.getSpeakingSectionForEdit(materialId);

            Material material = materialOpt.get();
            SpeakingSectionEditResult details = detailsOpt.orElse(null);
            MaterialDetailsUpsertedEvent event = MaterialDetailsUpsertedEvent.builder()
                    .materialId(material.getId())
                    .version(resolveVersion(material))
                    .materialTitle(resolveMaterialTitle(material, details))
                    .part1Title(details == null ? null : details.getPartTitle())
                    .part2Title(details == null ? null : details.getPart2Title())
                    .description(resolveDescription(material, details))
                    .updatedAt(resolveUpdatedAt(material))
                    .build();

            outboxPort.append(
                    UUID.randomUUID(),
                    "Material",
                    material.getId().toString(),
                    MATERIAL_DETAILS_UPSERTED,
                    MaterialDetailsUpsertedOutboxMessage.builder()
                            .requestId(requestId)
                            .event(event)
                            .build());
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

