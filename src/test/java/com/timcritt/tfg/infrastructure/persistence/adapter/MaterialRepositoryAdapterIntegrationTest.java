package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "material.kafka.outbox-relay.enabled=false",
        "authorization.classroom.enabled=false",
        "authorization.classroom.transport=http",
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.baseline-version=0.0",
        "spring.flyway.locations=classpath:db/migration",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class MaterialRepositoryAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("material_db")
            .withUsername("myuser")
            .withPassword("secret");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MaterialRepositoryAdapter materialRepositoryAdapter;

    @Test
    void findById_returnsFullyAssembledMaterialAggregate() {
        Material material = materialRepositoryAdapter.findById(10001L).orElseThrow();

        assertThat(material.getRoot()).isNotNull();
        assertThat(material.getRoot().getId()).isEqualTo(20100L);
        assertThat(material.getRoot().getChildren()).hasSize(2);

        MaterialNode part1 = material.getRoot().getChildren().get(0);
        MaterialNode part2 = material.getRoot().getChildren().get(1);

        assertThat(part1.getDisplayOrder()).isEqualTo(0);
        assertThat(part2.getDisplayOrder()).isEqualTo(1);
        assertThat(part1.getChildren()).hasSize(7);
        assertThat(part2.getChildren()).hasSize(4);
        assertThat(part1.getAssets()).hasSize(1);
        assertThat(part1.getChildren().get(0).getAssets()).hasSize(1);
    }
}
