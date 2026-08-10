package com.timcritt.tfg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
        // Disable trying to contact cloud config
          "spring.cloud.config.enabled=false",
          // Keep the smoke test fully local
          "eureka.client.enabled=false",
          // Disable gRPC transport so ClassroomAuthorizationGrpcConfig is not loaded
          "authorization.classroom.transport=http"
})
class TfgApplicationTests {

    @Test
    void contextLoads() {
    }

}
