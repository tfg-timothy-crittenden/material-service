package com.timcritt.tfg.application.service.materialdetails;

import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.integration.MaterialDetailsUpsertedOutboxMessage;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaterialDetailsRequestServiceTest {

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final TOEFLSpeakingNavigationUseCase navigationUseCase = mock(TOEFLSpeakingNavigationUseCase.class);
    private final IntegrationEventOutboxPort outboxPort = mock(IntegrationEventOutboxPort.class);

    private final MaterialDetailsRequestService service = new MaterialDetailsRequestService(
            materialRepository,
            navigationUseCase,
            outboxPort
    );

    @Test
    void processRequest_publishesEventsForFoundMaterialsAndSkipsMissingMaterials() {
        Material material10 = material(10L, 3L, "Section 10", "Desc 10", Instant.parse("2026-09-01T10:00:00Z"));
        Material material30 = material(30L, 7L, "Section 30", "Desc 30", Instant.parse("2026-09-01T12:00:00Z"));

        when(materialRepository.findById(10L)).thenReturn(Optional.of(material10));
        when(materialRepository.findById(20L)).thenReturn(Optional.empty());
        when(materialRepository.findById(30L)).thenReturn(Optional.of(material30));

        when(navigationUseCase.getSpeakingSectionForEdit(10L)).thenReturn(Optional.of(details("Material 10", "Part 1 A", "Part 2 A", "Desc 10")));
        when(navigationUseCase.getSpeakingSectionForEdit(30L)).thenReturn(Optional.of(details("Material 30", "Part 1 B", "Part 2 B", "Desc 30")));

        assertThatCode(() -> service.processRequest(MaterialDetailsRequestedPayload.builder()
                .requestId("req-1")
                .materialIds(List.of(10L, 20L, 30L))
                .build())).doesNotThrowAnyException();

        org.mockito.ArgumentCaptor<UUID> eventIdCaptor = org.mockito.ArgumentCaptor.forClass(UUID.class);
        org.mockito.ArgumentCaptor<String> aggregateTypeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> aggregateIdCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> eventTypeCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedOutboxMessage> payloadCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedOutboxMessage.class);
        verify(outboxPort, times(2)).append(
                eventIdCaptor.capture(),
                aggregateTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                eventTypeCaptor.capture(),
                payloadCaptor.capture());

        assertThat(eventIdCaptor.getAllValues()).allMatch(java.util.Objects::nonNull);
        assertThat(aggregateTypeCaptor.getAllValues()).containsExactly("Material", "Material");
        assertThat(aggregateIdCaptor.getAllValues()).containsExactly("10", "30");
        assertThat(eventTypeCaptor.getAllValues()).containsExactly("material.details.upserted.v1", "material.details.upserted.v1");
        assertThat(payloadCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedOutboxMessage::getRequestId)
                .containsExactly("req-1", "req-1");
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getMaterialId())
                .containsExactly(10L, 30L);
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getVersion())
                .containsExactly(3L, 7L);
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getMaterialTitle())
                .containsExactly("Material 10", "Material 30");
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getPart1Title())
                .containsExactly("Part 1 A", "Part 1 B");
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getPart2Title())
                .containsExactly("Part 2 A", "Part 2 B");
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.getEvent().getDescription())
                .containsExactly("Desc 10", "Desc 30");
    }

    @Test
    void processRequest_clampsNegativeVersionToZero() {
        Material material10 = material(10L, -3L, "Section 10", "Desc 10", Instant.parse("2026-09-01T10:00:00Z"));

        when(materialRepository.findById(10L)).thenReturn(Optional.of(material10));
        when(navigationUseCase.getSpeakingSectionForEdit(10L)).thenReturn(Optional.of(details("Material 10", "Part 1 A", "Part 2 A", "Desc 10")));

        service.processRequest(MaterialDetailsRequestedPayload.builder()
                .requestId("req-1")
                .materialIds(List.of(10L))
                .requestedAt(Instant.parse("2026-09-01T18:00:00Z"))
                .build());

        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedOutboxMessage> payloadCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedOutboxMessage.class);
        verify(outboxPort, times(1)).append(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("Material"), org.mockito.ArgumentMatchers.eq("10"), org.mockito.ArgumentMatchers.eq("material.details.upserted.v1"), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue().getRequestId()).isEqualTo("req-1");
        assertThat(payloadCaptor.getValue().getEvent().getVersion()).isEqualTo(0L);
    }

    @Test
    void processRequest_publishesFallbackWhenSpeakingDetailsMissing() {
        Material material10 = material(10L, 3L, "Section 10", "Desc 10", Instant.parse("2026-09-01T10:00:00Z"));

        when(materialRepository.findById(10L)).thenReturn(Optional.of(material10));
        when(navigationUseCase.getSpeakingSectionForEdit(10L)).thenReturn(Optional.empty());

        service.processRequest(MaterialDetailsRequestedPayload.builder()
                .requestId("req-1")
                .materialIds(List.of(10L))
                .requestedAt(Instant.parse("2026-09-01T18:00:00Z"))
                .build());

        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedOutboxMessage> payloadCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedOutboxMessage.class);
        verify(outboxPort, times(1)).append(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("Material"), org.mockito.ArgumentMatchers.eq("10"), org.mockito.ArgumentMatchers.eq("material.details.upserted.v1"), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue().getRequestId()).isEqualTo("req-1");
        assertThat(payloadCaptor.getValue().getEvent().getMaterialTitle()).isEqualTo("Section 10");
        assertThat(payloadCaptor.getValue().getEvent().getPart1Title()).isNull();
        assertThat(payloadCaptor.getValue().getEvent().getPart2Title()).isNull();
        assertThat(payloadCaptor.getValue().getEvent().getDescription()).isEqualTo("Desc 10");
    }

    private static Material material(Long id, Long version, String title, String description, Instant updatedAt) {
        return Material.builder()
                .id(id)
                .version(version)
                .title(title)
                .description(description)
                .status(MaterialStatus.PUBLISHED)
                .updatedAt(updatedAt)
                .build();
    }

    private static SpeakingSectionEditResult details(String materialTitle, String part1Title, String part2Title, String description) {
        return SpeakingSectionEditResult.builder()
                .materialTitle(materialTitle)
                .partTitle(part1Title)
                .part2Title(part2Title)
                .materialDescription(description)
                .build();
    }
}

