package com.timcritt.tfg.infrastructure.persistence.assembler;

import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialNodeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

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
class MaterialAggregateAssemblerIntegrationTest {

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
    private MaterialNodeJpaRepository materialNodeJpaRepository;

    @Autowired
    private MaterialAssetJpaRepository materialAssetJpaRepository;

    private final MaterialAggregateAssembler assembler = new MaterialAggregateAssembler();

    @Test
    void seededMaterial10001_canBeLoadedAsFlatRowsAndAssembledInMemory() {
        List<MaterialNodeJpaEntity> nodes = materialNodeJpaRepository.findByMaterialId(10001L);

        List<Long> nodeIds = nodes.stream()
                .map(MaterialNodeJpaEntity::getId)
                .toList();

        List<MaterialAssetEntity> assets = materialAssetJpaRepository.findByMaterialNode_IdIn(nodeIds);

        assertThat(nodeIds).containsExactlyInAnyOrder(
                20100L,
                20101L,
                20102L,
                20110L,
                20111L,
                20112L,
                20113L,
                20114L,
                20115L,
                20116L,
                20120L,
                20121L,
                20122L,
                20123L
        );
        assertThat(nodes).hasSize(14);
        assertThat(assets).hasSize(12);

        MaterialNode root = assembler.assembleRoot(20100L, nodes, assets);

        assertThat(root.getChildren()).hasSize(2);

        MaterialNode part1 = root.getChildren().get(0);
        MaterialNode part2 = root.getChildren().get(1);

        assertThat(part1.getDisplayOrder()).isEqualTo(0);
        assertThat(part2.getDisplayOrder()).isEqualTo(1);
        assertThat(part1.getChildren()).hasSize(7);
        assertThat(part2.getChildren()).hasSize(4);
        assertThat(part1.getAssets()).hasSize(1);
        assertThat(part1.getChildren().get(0).getAssets()).hasSize(1);
    }
}


