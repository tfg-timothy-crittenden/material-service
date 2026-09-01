package com.timcritt.tfg.infrastructure.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "messaging.kafka")
public class MessagingKafkaProperties {
    private boolean enabled = true;
}

