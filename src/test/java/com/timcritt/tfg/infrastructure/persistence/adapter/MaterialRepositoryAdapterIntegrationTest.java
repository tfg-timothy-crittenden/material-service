package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialStatus;
import com.timcritt.tfg.domain.model.AssetChange;
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

import java.time.Instant;

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

    @Autowired
    private MaterialAssetJpaRepository materialAssetJpaRepository;

    @Autowired
    private MaterialNodeJpaRepository materialNodeJpaRepository;

    @Test
    void findById_returnsFullyAssembledMaterialAggregate() {
        Material material = materialRepositoryAdapter.findById(10001L).orElseThrow();

        assertThat(material.getRoot()).isNotNull();
        assertThat(material.getRootId()).isEqualTo(20100L);
        assertThat(material.getRoot().getChildren()).hasSize(2);

        MaterialNode part1 = material.getRoot().getChildren().get(0);
        MaterialNode part2 = material.getRoot().getChildren().get(1);

        assertThat(part1.getDisplayOrder()).isEqualTo(0);
        assertThat(part2.getDisplayOrder()).isEqualTo(1);
        assertThat(part1.getChildren()).hasSize(7);
        assertThat(part2.getChildren()).hasSize(4);
        assertThat(part1.getAssets()).hasSize(1);
        assertThat(part1.getChildren().getFirst().getAssets()).hasSize(1);
    }

    @Test
    void save_persistsChangesMadeToChildNodesWithinTheMaterialAggregate() {
        Material material = materialRepositoryAdapter.findById(10001L).orElseThrow();

        assertThat(material.hasRoot()).isTrue();
        assertThat(material.getRoot()).isNotNull();
        assertThat(material.getRoot().getChildren()).hasSize(2);

        MaterialNode part1 = material.getRoot().getChildren().getFirst();
        String updatedTitle = part1.getTitle() + " (updated)";

        part1.updateTitle(updatedTitle);

        Material saved = materialRepositoryAdapter.save(material);

        assertThat(saved.hasRoot()).isTrue();
        assertThat(saved.getRoot()).isNotNull();
        assertThat(saved.getRoot().getChildren()).hasSize(2);
        assertThat(saved.getRoot().getChildren().getFirst().getTitle()).isEqualTo(updatedTitle);

        Material reloaded = materialRepositoryAdapter.findById(10001L).orElseThrow();

        assertThat(reloaded.getRoot().getChildren().getFirst().getTitle()).isEqualTo(updatedTitle);
    }

    @Test
    void save_assignsDatabaseGeneratedIdToNewMaterial() {
        Material material = Material.builder()
                .id(null)
                .examFamilyId(1L)
                .title("New draft material")
                .description("Created in test")
                .status(MaterialStatus.DRAFT)
                .version(0L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Material saved = materialRepositoryAdapter.save(material);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_persistsBrandNewMaterialWithTransientRootAndReturnsReassembledAggregate() {
        Material material = transientMaterial("Transient root material", "Created with transient root");
        MaterialNode root = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.SECTION,
                "Transient root material", 0);
        material.attachRoot(root);

        Material saved = materialRepositoryAdapter.save(material);
        Material reloaded = materialRepositoryAdapter.findById(saved.getId()).orElseThrow();
        var persistedNodes = materialNodeJpaRepository.findByMaterialId(saved.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);
        assertThat(saved.getTitle()).isEqualTo("Transient root material");
        assertThat(saved.getDescription()).isEqualTo("Created with transient root");
        assertThat(saved.getRoot()).isNotNull();
        assertThat(saved.getRoot().getId()).isNotNull();
        assertThat(saved.getRoot().getMaterialId()).isEqualTo(saved.getId());
        assertThat(saved.getRoot().getParentNodeId()).isNull();
        assertThat(saved.getRootId()).isEqualTo(saved.getRoot().getId());
        assertThat(saved.getRoot().getKind()).isEqualTo(com.timcritt.tfg.domain.model.MaterialNodeKind.SECTION);
        assertThat(saved.getRoot().getTitle()).isEqualTo("Transient root material");
        assertThat(saved.getRoot().getDisplayOrder()).isEqualTo(0);
        assertThat(saved.getRoot().getConfig()).isEmpty();
        assertThat(saved.getRoot().getVersion()).isEqualTo(0L);

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getVersion()).isEqualTo(0L);
        assertThat(reloaded.getRoot()).isNotNull();
        assertThat(reloaded.getRoot().getId()).isEqualTo(saved.getRoot().getId());
        assertThat(reloaded.getRoot().getMaterialId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRoot().getParentNodeId()).isNull();
        assertThat(reloaded.getRootId()).isEqualTo(reloaded.getRoot().getId());
        assertThat(reloaded.getRoot().getVersion()).isEqualTo(0L);

        assertThat(persistedNodes)
                .filteredOn(node -> node.getParentNodeId() == null)
                .singleElement()
                .satisfies(rootNode -> {
                    assertThat(rootNode.getId()).isEqualTo(saved.getRoot().getId());
                    assertThat(rootNode.getMaterialId()).isEqualTo(saved.getId());
                });
    }

    @Test
    void save_persistsBrandNewMaterialWithTransientRootAndChildren() {
        Material material = transientMaterial("Structured material", "Root and children");
        MaterialNode root = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.SECTION,
                "Structured material", 0);
        MaterialNode part1 = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.PART,
                "Part 1", 0);
        MaterialNode part2 = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.PART,
                "Part 2", 1);
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        Material saved = materialRepositoryAdapter.save(material);
        Material reloaded = materialRepositoryAdapter.findById(saved.getId()).orElseThrow();
        var persistedNodes = materialNodeJpaRepository.findByMaterialId(saved.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);
        assertThat(saved.getRoot()).isNotNull();
        assertThat(saved.getRoot().getId()).isNotNull();
        assertThat(saved.getRoot().getMaterialId()).isEqualTo(saved.getId());
        assertThat(saved.getRoot().getChildren()).hasSize(2);
        assertThat(saved.getRoot().childAt(0).getId()).isNotNull();
        assertThat(saved.getRoot().childAt(1).getId()).isNotNull();
        assertThat(saved.getRoot().childAt(0).getMaterialId()).isEqualTo(saved.getId());
        assertThat(saved.getRoot().childAt(1).getMaterialId()).isEqualTo(saved.getId());
        assertThat(saved.getRoot().childAt(0).getParentNodeId()).isEqualTo(saved.getRoot().getId());
        assertThat(saved.getRoot().childAt(1).getParentNodeId()).isEqualTo(saved.getRoot().getId());
        assertThat(saved.getRoot().childAt(0).getDisplayOrder()).isEqualTo(0);
        assertThat(saved.getRoot().childAt(1).getDisplayOrder()).isEqualTo(1);
        assertThat(saved.getRoot().childAt(0).getVersion()).isEqualTo(0L);
        assertThat(saved.getRoot().childAt(1).getVersion()).isEqualTo(0L);

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRootId()).isEqualTo(saved.getRootId());
        assertThat(reloaded.getRoot().getChildren()).hasSize(2);
        assertThat(reloaded.getRoot().childAt(0).getMaterialId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRoot().childAt(1).getMaterialId()).isEqualTo(saved.getId());
        assertThat(reloaded.getRoot().childAt(0).getParentNodeId()).isEqualTo(reloaded.getRoot().getId());
        assertThat(reloaded.getRoot().childAt(1).getParentNodeId()).isEqualTo(reloaded.getRoot().getId());
        assertThat(reloaded.getRoot().childAt(0).getTitle()).isEqualTo("Part 1");
        assertThat(reloaded.getRoot().childAt(1).getTitle()).isEqualTo("Part 2");

        assertThat(persistedNodes)
                .filteredOn(node -> node.getParentNodeId() == null)
                .hasSize(1);
    }

    @Test
    void save_persistsBrandNewMaterialWithTransientGrandchildRelationships() {
        Material material = transientMaterial("Grandchild material", "Recursive persistence");
        MaterialNode root = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.SECTION,
                "Grandchild material", 0);
        MaterialNode part1 = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.PART,
                "Part 1", 0);
        MaterialNode question1 = transientNode(null, null, com.timcritt.tfg.domain.model.MaterialNodeKind.ITEM,
                "Question 1", 0);
        part1.addChild(question1);
        root.addChild(part1);
        material.attachRoot(root);

        Material saved = materialRepositoryAdapter.save(material);
        Material reloaded = materialRepositoryAdapter.findById(saved.getId()).orElseThrow();

        MaterialNode savedRoot = saved.getRoot();
        MaterialNode savedPart1 = savedRoot.childAt(0);
        MaterialNode savedQuestion1 = savedPart1.childAt(0);
        MaterialNode reloadedRoot = reloaded.getRoot();
        MaterialNode reloadedPart1 = reloadedRoot.childAt(0);
        MaterialNode reloadedQuestion1 = reloadedPart1.childAt(0);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);
        assertThat(savedRoot.getId()).isNotNull();
        assertThat(savedPart1.getId()).isNotNull();
        assertThat(savedQuestion1.getId()).isNotNull();
        assertThat(savedPart1.getMaterialId()).isEqualTo(saved.getId());
        assertThat(savedQuestion1.getMaterialId()).isEqualTo(saved.getId());
        assertThat(savedPart1.getParentNodeId()).isEqualTo(savedRoot.getId());
        assertThat(savedQuestion1.getParentNodeId()).isEqualTo(savedPart1.getId());
        assertThat(savedQuestion1.getKind()).isEqualTo(com.timcritt.tfg.domain.model.MaterialNodeKind.ITEM);
        assertThat(savedQuestion1.getTitle()).isEqualTo("Question 1");
        assertThat(savedQuestion1.getDisplayOrder()).isEqualTo(0);
        assertThat(savedQuestion1.getConfig()).isEmpty();
        assertThat(savedQuestion1.getVersion()).isEqualTo(0L);

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getVersion()).isEqualTo(0L);
        assertThat(reloadedQuestion1.getMaterialId()).isEqualTo(reloaded.getId());
        assertThat(reloadedQuestion1.getParentNodeId()).isEqualTo(reloadedPart1.getId());
        assertThat(reloadedPart1.getParentNodeId()).isEqualTo(reloadedRoot.getId());
        assertThat(reloadedQuestion1.getTitle()).isEqualTo("Question 1");
    }

    @Test
    void save_persistsExistingAttachedAssetModifiedThroughTheAggregate() {
        Long materialId = 10001L;
        Material material = materialRepositoryAdapter.findById(materialId).orElseThrow();

        MaterialNode question = material.getRoot().childAt(0).childAt(0);
        MaterialAsset originalAsset = question.getAssets().getFirst();
        Long nodeId = question.getId();
        Long assetId = originalAsset.getId();
        String oldStorageKey = originalAsset.getStorageKey();
        Long oldAssetVersion = originalAsset.getVersion();

        AssetChange change = material.replaceNodeAssetFile(
                nodeId,
                assetId,
                "replacement-key",
                "replacement.mp3",
                "audio/mpeg",
                12345L
        );

        assertThat(change.previousStorageKey()).isEqualTo(oldStorageKey);
        assertThat(change.currentStorageKey()).isEqualTo("replacement-key");
        assertThat(originalAsset.getStorageKey()).isEqualTo("replacement-key");
        assertThat(originalAsset.getOriginalFilename()).isEqualTo("replacement.mp3");
        assertThat(originalAsset.getMimeType()).isEqualTo("audio/mpeg");
        assertThat(originalAsset.getFileSizeBytes()).isEqualTo(12345L);
        assertThat(originalAsset.getVersion()).isEqualTo(oldAssetVersion + 1);

        materialRepositoryAdapter.save(material);

        Material reloaded = materialRepositoryAdapter.findById(materialId).orElseThrow();
        MaterialNode reloadedQuestion = reloaded.getRoot().childAt(0).childAt(0);
        MaterialAsset reloadedAsset = reloadedQuestion.getAssets().stream()
                .filter(asset -> assetId.equals(asset.getId()))
                .findFirst()
                .orElseThrow();
        var assetRowsForNode = materialAssetJpaRepository.findByMaterialNode_Id(nodeId);

        assertThat(reloadedQuestion.getId()).isEqualTo(nodeId);
        assertThat(reloadedQuestion.getAssets()).hasSize(1);
        assertThat(reloadedAsset.getId()).isEqualTo(assetId);
        assertThat(reloadedAsset.getMaterialNodeId()).isEqualTo(nodeId);
        assertThat(assetRowsForNode)
                .extracting(asset -> asset.getId())
                .containsExactly(assetId);
        assertThat(reloaded.getVersion()).isEqualTo(material.getVersion());
        assertThat(reloadedAsset.getStorageKey()).isEqualTo("replacement-key");
        assertThat(reloadedAsset.getOriginalFilename()).isEqualTo("replacement.mp3");
        assertThat(reloadedAsset.getMimeType()).isEqualTo("audio/mpeg");
        assertThat(reloadedAsset.getFileSizeBytes()).isEqualTo(12345L);
        assertThat(reloadedAsset.getVersion()).isEqualTo(oldAssetVersion + 1);
    }

    @Test
    void save_insertsNewAttachedAssetAddedThroughTheAggregate() {
        Long materialId = 10001L;
        Material material = materialRepositoryAdapter.findById(materialId).orElseThrow();

        MaterialNode part1 = material.getRoot().childAt(0);
        Long nodeId = part1.getId();
        String storageKey = "aggregate-inserted-key";

        material.addNodeAsset(
                nodeId,
                MaterialAsset.Kind.OTHER,
                storageKey,
                "attachment.bin",
                "application/octet-stream",
                77L
        );

        Material saved = materialRepositoryAdapter.save(material);
        MaterialNode savedPart1 = saved.getRoot().childAt(0);
        MaterialAsset savedAsset = savedPart1.getAssets().stream()
                .filter(asset -> storageKey.equals(asset.getStorageKey()))
                .findFirst()
                .orElseThrow();
        var assetRowsForNode = materialAssetJpaRepository.findByMaterialNode_Id(nodeId);
        var insertedRows = assetRowsForNode.stream()
                .filter(asset -> storageKey.equals(asset.getStorageKey()))
                .toList();

        assertThat(insertedRows).singleElement().satisfies(assetRow -> {
            assertThat(assetRow.getId()).isNotNull();
            assertThat(assetRow.getMaterialNode().getId()).isEqualTo(nodeId);
            assertThat(assetRow.getKind()).isEqualTo(com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity.Kind.OTHER);
            assertThat(assetRow.getOriginalFilename()).isEqualTo("attachment.bin");
            assertThat(assetRow.getMimeType()).isEqualTo("application/octet-stream");
            assertThat(assetRow.getFileSizeBytes()).isEqualTo(77L);
            assertThat(assetRow.getVersion()).isEqualTo(0L);
        });
        assertThat(savedAsset.getId()).isNotNull();
        assertThat(savedAsset.getMaterialNodeId()).isEqualTo(nodeId);
        assertThat(savedAsset.getKind()).isEqualTo(MaterialAsset.Kind.OTHER);
        assertThat(savedAsset.getStorageKey()).isEqualTo(storageKey);
        assertThat(savedAsset.getOriginalFilename()).isEqualTo("attachment.bin");
        assertThat(savedAsset.getMimeType()).isEqualTo("application/octet-stream");
        assertThat(savedAsset.getFileSizeBytes()).isEqualTo(77L);
        assertThat(savedAsset.getVersion()).isEqualTo(0L);

        Material reloaded = materialRepositoryAdapter.findById(materialId).orElseThrow();
        MaterialAsset reloadedAsset = reloaded.getRoot().childAt(0).getAssets().stream()
                .filter(asset -> storageKey.equals(asset.getStorageKey()))
                .findFirst()
                .orElseThrow();

        assertThat(reloadedAsset.getId()).isEqualTo(savedAsset.getId());
        assertThat(reloaded.getVersion()).isEqualTo(material.getVersion());
        assertThat(reloaded.getRoot().childAt(0).getAssets().stream()
                .filter(asset -> storageKey.equals(asset.getStorageKey())))
                .hasSize(1);
    }

    @Test
    void save_deletesPersistedAssetRemovedFromTheAggregate() {
        Long materialId = 10001L;
        Material material = materialRepositoryAdapter.findById(materialId).orElseThrow();

        MaterialNode part1 = material.getRoot().childAt(0);
        MaterialNode firstQuestion = part1.childAt(0);
        MaterialAsset removedAsset = part1.getAssets().getFirst();
        MaterialAsset unrelatedAsset = firstQuestion.getAssets().getFirst();
        Long removedAssetId = removedAsset.getId();
        Long unrelatedAssetId = unrelatedAsset.getId();

        material.removeNodeAsset(part1.getId(), removedAssetId);

        Material saved = materialRepositoryAdapter.save(material);
        Material reloaded = materialRepositoryAdapter.findById(materialId).orElseThrow();
        MaterialNode reloadedPart1 = reloaded.getRoot().childAt(0);
        MaterialNode reloadedFirstQuestion = reloadedPart1.childAt(0);

        assertThat(saved.getVersion()).isEqualTo(material.getVersion());
        assertThat(reloaded.getVersion()).isEqualTo(material.getVersion());
        assertThat(reloadedPart1.getAssets())
                .noneMatch(asset -> removedAssetId.equals(asset.getId()));
        assertThat(materialAssetJpaRepository.findById(removedAssetId)).isEmpty();
        assertThat(reloadedFirstQuestion.getAssets())
                .anyMatch(asset -> unrelatedAssetId.equals(asset.getId()));
        assertThat(materialAssetJpaRepository.findById(unrelatedAssetId)).isPresent();
    }

    private static Material transientMaterial(String title, String description) {
        return Material.builder()
                .id(null)
                .examFamilyId(1L)
                .title(title)
                .description(description)
                .status(MaterialStatus.DRAFT)
                .version(0L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static MaterialNode transientNode(
            Long materialId,
            Long parentNodeId,
            com.timcritt.tfg.domain.model.MaterialNodeKind kind,
            String title,
            int displayOrder
    ) {
        return MaterialNode.builder()
                .id(null)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind(kind)
                .title(title)
                .displayOrder(displayOrder)
                .skillId(4L)
                .responseMode(kind == com.timcritt.tfg.domain.model.MaterialNodeKind.ITEM ? "SPOKEN" : "NONE")
                .responseRequired(kind == com.timcritt.tfg.domain.model.MaterialNodeKind.ITEM)
                .scoringMode("NONE")
                .config(java.util.Map.of())
                .version(0L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
