package com.timcritt.tfg.infrastructure.config;

import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.service.materialdetails.MaterialDetailsRequestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaterialDetailsConfig {

    @Bean
    MaterialDetailsRequestService materialDetailsRequestService(
            MaterialRepositoryPort materialRepository,
            TOEFLSpeakingNavigationUseCase navigationUseCase,
            IntegrationEventOutboxPort outboxPort) {
        return new MaterialDetailsRequestService(materialRepository, navigationUseCase, outboxPort);
    }
}

