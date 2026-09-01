package com.timcritt.tfg.application.service.materialdetails;

import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialDetailsUpsertedEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.domain.event.MaterialDetailsRequestedPayload;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaterialDetailsRequestServiceTest {

    private final MaterialRepositoryPort materialRepository = mock(MaterialRepositoryPort.class);
    private final TOEFLSpeakingNavigationUseCase navigationUseCase = mock(TOEFLSpeakingNavigationUseCase.class);
    private final MaterialDetailsUpsertedEventPublisherPort eventPublisher = mock(MaterialDetailsUpsertedEventPublisherPort.class);

    private final MaterialDetailsRequestService service = new MaterialDetailsRequestService(
            materialRepository,
            navigationUseCase,
            eventPublisher
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

        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedEvent> eventCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedEvent.class);
        org.mockito.ArgumentCaptor<String> requestIdCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(eventPublisher, times(2)).publishMaterialDetailsUpserted(eventCaptor.capture(), requestIdCaptor.capture());

        assertThat(requestIdCaptor.getAllValues()).containsExactly("req-1", "req-1");
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getMaterialId)
                .containsExactly(10L, 30L);
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getVersion)
                .containsExactly(3L, 7L);
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getMaterialTitle)
                .containsExactly("Material 10", "Material 30");
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getPart1Title)
                .containsExactly("Part 1 A", "Part 1 B");
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getPart2Title)
                .containsExactly("Part 2 A", "Part 2 B");
        assertThat(eventCaptor.getAllValues())
                .extracting(MaterialDetailsUpsertedEvent::getDescription)
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

        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedEvent> eventCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedEvent.class);
        org.mockito.ArgumentCaptor<String> requestIdCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(eventPublisher, times(1)).publishMaterialDetailsUpserted(eventCaptor.capture(), requestIdCaptor.capture());

        assertThat(eventCaptor.getValue().getVersion()).isEqualTo(0L);
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

        org.mockito.ArgumentCaptor<MaterialDetailsUpsertedEvent> eventCaptor = org.mockito.ArgumentCaptor.forClass(MaterialDetailsUpsertedEvent.class);
        org.mockito.ArgumentCaptor<String> requestIdCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(eventPublisher, times(1)).publishMaterialDetailsUpserted(eventCaptor.capture(), requestIdCaptor.capture());

        assertThat(eventCaptor.getValue().getMaterialTitle()).isEqualTo("Section 10");
        assertThat(eventCaptor.getValue().getPart1Title()).isNull();
        assertThat(eventCaptor.getValue().getPart2Title()).isNull();
        assertThat(eventCaptor.getValue().getDescription()).isEqualTo("Desc 10");
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

