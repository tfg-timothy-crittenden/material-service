package com.timcritt.tfg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {
        // Use H2 in-memory database for tests so DataSource auto-config succeeds
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        // Disable Flyway during the lightweight context load test
        "spring.flyway.enabled=false",
          // No schema creation is needed for this smoke test
          "spring.jpa.hibernate.ddl-auto=none",
        "spring.kafka.listener.auto-startup=false",
        "material.kafka.outbox-relay.enabled=false",
        "authorization.classroom.enabled=false",
        // Disable trying to contact cloud config
          "spring.cloud.config.enabled=false",
          // Keep the smoke test fully local
          "eureka.client.enabled=false"
})
        @TestPropertySource(properties = {"authorization.classroom.enabled=false", "material.kafka.outbox-relay.enabled=false"})
class TfgApplicationTests {


    @Test
    void contextLoads() {
    }

}
