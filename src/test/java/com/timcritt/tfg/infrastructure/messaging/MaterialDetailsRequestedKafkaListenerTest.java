package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.integration.MaterialDetailsUpsertedOutboxMessage;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.service.materialdetails.MaterialDetailsRequestService;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MaterialDetailsRequestedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private static final String REQUEST_ID = "6e358158-9f6c-44dc-a2e8-0bcd7646c302";

    @Test
    void validRequest_publishesExpectedUpsertEvents() throws Exception {
        MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
        TOEFLSpeakingNavigationUseCase navigationUseCase = mock(TOEFLSpeakingNavigationUseCase.class);
        IntegrationEventOutboxPort outboxPort = mock(IntegrationEventOutboxPort.class);

        Material material1 = material(101L, 5L, "TOEFL Speaking 101", "Desc 101", Instant.parse("2026-09-01T10:00:00Z"));
        Material material2 = material(202L, 9L, "TOEFL Speaking 202", "Desc 202", Instant.parse("2026-09-01T11:00:00Z"));

        when(materialRepository.findById(101L)).thenReturn(Optional.of(material1));
        when(materialRepository.findById(202L)).thenReturn(Optional.of(material2));
        when(navigationUseCase.getSpeakingSectionForEdit(101L)).thenReturn(Optional.of(details("TOEFL Speaking 101", "Part 1 A", "Part 2 A", "Desc 101")));
        when(navigationUseCase.getSpeakingSectionForEdit(202L)).thenReturn(Optional.of(details("TOEFL Speaking 202", "Part 1 B", "Part 2 B", "Desc 202")));

        MaterialDetailsRequestService service = new MaterialDetailsRequestService(materialRepository, navigationUseCase, outboxPort);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        MaterialDetailsRequestedPayload payload = MaterialDetailsRequestedPayload.builder()
                .requestId(REQUEST_ID)
                .materialIds(List.of(101L, 202L))
                .requestedAt(Instant.parse("2026-09-01T18:00:00Z"))
                .build();

        listener.onMessage(objectMapper.writeValueAsString(payload));

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

        assertThat(eventIdCaptor.getAllValues()).allSatisfy(id -> assertThat(id).isNotNull());
        assertThat(aggregateTypeCaptor.getAllValues()).containsExactly("Material", "Material");
        assertThat(aggregateIdCaptor.getAllValues()).containsExactly("101", "202");
        assertThat(eventTypeCaptor.getAllValues()).containsExactly("material.details.upserted.v1", "material.details.upserted.v1");
        assertThat(payloadCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedOutboxMessage::getRequestId)
                .containsExactly(REQUEST_ID, REQUEST_ID);
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getMaterialId())
                .containsExactly(101L, 202L);
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getVersion())
                .containsExactly(5L, 9L);
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getMaterialTitle())
                .containsExactly("TOEFL Speaking 101", "TOEFL Speaking 202");
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getPart1Title())
                .containsExactly("Part 1 A", "Part 1 B");
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getPart2Title())
                .containsExactly("Part 2 A", "Part 2 B");
        assertThat(payloadCaptor.getAllValues())
                .extracting(outboxMessage -> outboxMessage.getEvent().getDescription())
                .containsExactly("Desc 101", "Desc 202");
    }

    @Test
    void malformedJson_doesNotReachService() {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        listener.onMessage("{not-json");

        verifyNoInteractions(service);
    }

    @Test
    void missingRequestId_doesNotReachService() {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        String payload = """
                {"materialIds":[1,2],"requestedAt":"2026-09-01T18:00:00Z"}
                """;

        listener.onMessage(payload);

        verifyNoInteractions(service);
    }

    @Test
    void invalidMaterialIds_doesNotReachService() {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        String emptyIdsPayload = """
                {"requestId":"request-123","materialIds":[],"requestedAt":"2026-09-01T18:00:00Z"}
                """;
        listener.onMessage(emptyIdsPayload);
        verifyNoInteractions(service);

        String invalidIdsPayload = """
                {"requestId":"request-123","materialIds":[1,0,-3],"requestedAt":"2026-09-01T18:00:00Z"}
                """;
        listener.onMessage(invalidIdsPayload);
        verifyNoInteractions(service);
    }

    @Test
    void missingRequestedAt_doesNotReachService() {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        String payload = """
                {"requestId":"request-123","materialIds":[1,2]}
                """;

        listener.onMessage(payload);

        verifyNoInteractions(service);
    }

    @Test
    void duplicateMaterialIds_doesNotReachService() throws Exception {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, enabledMessagingKafkaProperties());

        MaterialDetailsRequestedPayload payload = MaterialDetailsRequestedPayload.builder()
                .requestId(REQUEST_ID)
                .materialIds(List.of(1L, 1L, 2L))
                .requestedAt(Instant.parse("2026-09-01T18:00:00Z"))
                .build();

        listener.onMessage(objectMapper.writeValueAsString(payload));

        verifyNoInteractions(service);
    }

    @Test
    void disabledMessagingKafka_doesNotReachService() throws Exception {
        MaterialDetailsRequestService service = mock(MaterialDetailsRequestService.class);
        MessagingKafkaProperties properties = new MessagingKafkaProperties();
        properties.setEnabled(false);
        MaterialDetailsRequestedKafkaListener listener = new MaterialDetailsRequestedKafkaListener(objectMapper, service, properties);

        MaterialDetailsRequestedPayload payload = MaterialDetailsRequestedPayload.builder()
                .requestId(REQUEST_ID)
                .materialIds(List.of(101L))
                .requestedAt(Instant.parse("2026-09-01T18:00:00Z"))
                .build();

        listener.onMessage(objectMapper.writeValueAsString(payload));

        verifyNoInteractions(service);
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

    private static MessagingKafkaProperties enabledMessagingKafkaProperties() {
        MessagingKafkaProperties properties = new MessagingKafkaProperties();
        properties.setEnabled(true);
        return properties;
    }
}

