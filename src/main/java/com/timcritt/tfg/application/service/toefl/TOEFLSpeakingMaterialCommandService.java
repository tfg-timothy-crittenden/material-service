package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.application.port.outbound.*;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialStatus;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.time.Instant;
import java.time.OffsetDateTime;

import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DELETED;

public class TOEFLSpeakingMaterialCommandService implements TOEFLSpeakingMaterialCommandUseCase {
    private static final Long TOEFL_SKILL_ID = 4L;
    private static final Long TOEFL_EXAM_FAMILY_ID = 1L;
    private static final int PART_1_QUESTION_COUNT = 7;
    private static final int PART_2_QUESTION_COUNT = 4;

    private final MaterialRepositoryPort materialRepository;
    private final MaterialNodeRepositoryPort materialNodeRepository;
    private final MaterialAssetRepositoryPort materialAssetRepository;
    private final StorageRepositoryPort storageRepositoryPort;
    private final IntegrationEventOutboxPort outboxPort;

    public TOEFLSpeakingMaterialCommandService(
            MaterialRepositoryPort materialRepository,
            MaterialNodeRepositoryPort materialNodeRepository,
            MaterialAssetRepositoryPort materialAssetRepository,
            StorageRepositoryPort storageRepositoryPort,
            IntegrationEventOutboxPort outboxPort) {
        this.materialRepository = materialRepository;
        this.materialNodeRepository = materialNodeRepository;
        this.materialAssetRepository = materialAssetRepository;
        this.storageRepositoryPort = storageRepositoryPort;
        this.outboxPort = outboxPort;
    }

    @Override
    public Long uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload is required");
        }

        // Auto-fill title so the DB NOT NULL constraint is always satisfied for drafts.
        String effectiveTitle = hasText(command.getMaterialTitle())
                ? command.getMaterialTitle()
                : "Untitled Draft";

        Material savedMaterial = createMaterial(effectiveTitle, command.getMaterialDescription());
        MaterialNode savedRootNode = createSectionRoot(savedMaterial.getId(), effectiveTitle);
        savedMaterial.setMaterialNodeId(savedRootNode.getId());
        materialRepository.save(savedMaterial);

        // Always create Part 1 and Part 2 to scaffold the full tree structure.
        MaterialNode part1Node = createPartNode(savedMaterial.getId(), savedRootNode.getId(), command.getPartTitle(), 0);
        saveImageAsset(command.getPartImage(), savedMaterial.getId(), part1Node.getId());
        createQuestions(savedMaterial.getId(), part1Node.getId(), command.getQuestions(), 1);
        createMissingPlaceholderQuestions(savedMaterial.getId(), part1Node.getId(), safeSize(command.getQuestions()), PART_1_QUESTION_COUNT);

        MaterialNode part2Node = createPartNode(savedMaterial.getId(), savedRootNode.getId(), command.getPart2Title(), 1);
        createQuestions(savedMaterial.getId(), part2Node.getId(), command.getPart2Questions(), 2);
        createMissingPlaceholderQuestions(savedMaterial.getId(), part2Node.getId(), safeSize(command.getPart2Questions()), PART_2_QUESTION_COUNT);

        return savedMaterial.getId();
    }

    @Override
    public void publishSpeakingSection(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));

        if (material.getStatus() == MaterialStatus.PUBLISHED) {
            return; // idempotent – already published
        }

        // ── Completeness validation ───────────────────────────────────────────
        if (!hasText(material.getTitle()) || "Untitled Draft".equals(material.getTitle())) {
            throw new IllegalStateException("Cannot publish: material title is required");
        }

        MaterialNode rootNode = materialNodeRepository.findById(material.getMaterialNodeId())
                .orElseThrow(() -> new IllegalStateException("Cannot publish: section node not found"));

        MaterialNode part1 = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 0)
                .orElseThrow(() -> new IllegalStateException("Cannot publish: Part 1 is missing"));

        if (!hasText(part1.getTitle())) {
            throw new IllegalStateException("Cannot publish: Part 1 title is required");
        }

        boolean part1HasImage = materialAssetRepository.findByMaterialNodeId(part1.getId()).stream()
                .anyMatch(a -> a.getKind() == MaterialAsset.Kind.IMAGE);
        if (!part1HasImage) {
            throw new IllegalStateException("Cannot publish: Part 1 image is required");
        }

        List<MaterialNode> part1Questions = materialNodeRepository.findByParentNodeId(part1.getId());
        if (part1Questions.isEmpty()) {
            throw new IllegalStateException("Cannot publish: Part 1 must have at least one question");
        }
        for (MaterialNode q : part1Questions) {
            if (!hasText(q.getTranscriptText())) {
                throw new IllegalStateException("Cannot publish: all Part 1 questions must have transcript text");
            }
            boolean hasAudio = materialAssetRepository.findByMaterialNodeId(q.getId()).stream()
                    .anyMatch(a -> a.getKind() == MaterialAsset.Kind.AUDIO);
            if (!hasAudio) {
                throw new IllegalStateException("Cannot publish: Part 1 question " + q.getDisplayOrder() + " is missing audio");
            }
        }

        MaterialNode part2 = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 1)
                .orElseThrow(() -> new IllegalStateException("Cannot publish: Part 2 is missing"));

        if (!hasText(part2.getTitle())) {
            throw new IllegalStateException("Cannot publish: Part 2 title is required");
        }

        List<MaterialNode> part2Questions = materialNodeRepository.findByParentNodeId(part2.getId());
        if (part2Questions.size() != 4) {
            throw new IllegalStateException("Cannot publish: Part 2 must have exactly 4 questions (found " + part2Questions.size() + ")");
        }
        for (MaterialNode q : part2Questions) {
            if (!hasText(q.getTranscriptText())) {
                throw new IllegalStateException("Cannot publish: all Part 2 questions must have transcript text");
            }
            boolean hasAudio = materialAssetRepository.findByMaterialNodeId(q.getId()).stream()
                    .anyMatch(a -> a.getKind() == MaterialAsset.Kind.AUDIO);
            if (!hasAudio) {
                throw new IllegalStateException("Cannot publish: Part 2 question " + q.getDisplayOrder() + " is missing audio");
            }
        }

        // ── Flip status ───────────────────────────────────────────────────────
        material.setStatus(MaterialStatus.PUBLISHED);
        material.setUpdatedAt(Instant.now());
        material.setVersion(material.getVersion() + 1);
        materialRepository.save(material);
    }

    @Override
    public void deleteSpeakingSection(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required");
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));

        Long rootNodeId = material.getMaterialNodeId();
        if (rootNodeId != null) {
            List<Long> nodeIds = collectSubtreeNodeIds(rootNodeId);
            List<String> storageKeys = new ArrayList<>();

            for (Long nodeId : nodeIds) {
                for (MaterialAsset asset : materialAssetRepository.findByMaterialNodeId(nodeId)) {
                    if (hasText(asset.getStorageKey())) {
                        storageKeys.add(asset.getStorageKey());
                    }
                }
            }

            // DB-level ON DELETE CASCADE removes children and assets from relational tables.
            materialNodeRepository.deleteById(rootNodeId);

            // Best-effort object-store cleanup; never fail DB delete because object cleanup failed.
            for (String key : storageKeys) {
                try {
                    storageRepositoryPort.deleteObject("toefl", key);
                } catch (Exception ex) {
                    // Intentionally swallowed; stale objects can be cleaned up later.
                }
            }
        }

        materialRepository.delete(materialId);

        Instant deletedAt = Instant.now();
        outboxPort.append(
                UUID.randomUUID(),
                "Material",
                material.getId().toString(),
                MATERIAL_DELETED,
                MaterialDeletedEvent.builder()
                        .materialId(materialId)
                        .rootNodeId(rootNodeId)
                        .deletedAt(deletedAt)
                        .build()
        );
    }


    private MaterialNode createSectionRoot(Long materialId, String sectionTitle) {
        Instant now = Instant.now();
        MaterialNode sectionNode = MaterialNode.builder()
                .id(null)
                .materialId(materialId)
                .parentNodeId(null)
                .kind("SECTION")
                .title(sectionTitle)
                .displayOrder(0)
                .skillId(TOEFL_SKILL_ID)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return materialNodeRepository.save(sectionNode);
    }

    private Material createMaterial(String materialTitle, String materialDescription) {
        Instant now = Instant.now();
        Material material = Material.builder()
                .id(null)
                .examFamilyId(TOEFL_EXAM_FAMILY_ID)
                .materialNodeId(null)
                .title(materialTitle)
                .description(materialDescription)
                .authorId(null)
                .ownerOrgId(null)
                .status(MaterialStatus.DRAFT)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return materialRepository.save(material);
    }

    private MaterialNode createPartNode(Long materialId, Long parentNodeId, String title, int displayOrder) {
        Instant now = Instant.now();
        MaterialNode partNode = MaterialNode.builder()
                .id(null)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind("PART")
                .title(title)
                .displayOrder(displayOrder)
                .skillId(TOEFL_SKILL_ID)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return materialNodeRepository.save(partNode);
    }

    private void saveImageAsset(UploadedFileCommand image, Long materialId, Long materialNodeId) {
        saveAsset(image, materialNodeId, MaterialAsset.Kind.IMAGE,
                buildSpeakingStorageKey(materialId, 1, MaterialAsset.Kind.IMAGE, null));
    }

    private void saveAudioAsset(UploadedFileCommand audio, Long materialId, Long materialNodeId, int partNumber, int questionNumber) {
        saveAsset(audio, materialNodeId, MaterialAsset.Kind.AUDIO,
                buildSpeakingStorageKey(materialId, partNumber, MaterialAsset.Kind.AUDIO, questionNumber));
    }

    private void saveAsset(UploadedFileCommand file, Long materialNodeId, MaterialAsset.Kind kind, String storageKey) {
        if (file == null || file.getBytes() == null || file.getBytes().length == 0) {
            return;
        }

        try {
            storageRepositoryPort.uploadObject("toefl", storageKey, new ByteArrayInputStream(file.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to storage", e);
        }

        String originalFilename = hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";

        MaterialAsset asset = new MaterialAsset();
        asset.setMaterialNodeId(materialNodeId);
        asset.setKind(kind);
        asset.setStorageKey(storageKey);
        asset.setOriginalFilename(originalFilename);
        asset.setMimeType(file.getContentType());
        asset.setFileSizeBytes(file.getSize());
        asset.setDisplayOrder(0);
        asset.setVersion(0L);
        asset.setMetadata(new HashMap<>());
        OffsetDateTime now = OffsetDateTime.now();
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);
        materialAssetRepository.save(asset);
    }

    private void createQuestions(Long materialId, Long partNodeId, List<SpeakingQuestionUploadCommand> questions, int partNumber) {
        Instant now = Instant.now();
        List<SpeakingQuestionUploadCommand> safeQuestions = questions == null ? Collections.emptyList() : new ArrayList<>(questions);

        int questionOrder = 0;
        for (SpeakingQuestionUploadCommand question : safeQuestions) {
            String title = "Question " + (questionOrder + 1);
            MaterialNode questionNode = MaterialNode.builder()
                    .id(null)
                    .materialId(materialId)
                    .parentNodeId(partNodeId)
                    .kind("ITEM")
                    .title(title)
                    .displayOrder(questionOrder)
                    .skillId(TOEFL_SKILL_ID)
                    .transcriptText(question.getTranscriptText())
                    .responseMode("SPOKEN")
                    .responseRequired(true)
                    .scoringMode("NONE")
                    // Part 2 config is optional; default to empty object when omitted.
                    .config(question.getConfig() == null ? new HashMap<>() : question.getConfig())
                    .version(0L)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            MaterialNode savedQuestionNode = materialNodeRepository.save(questionNode);
            saveAudioAsset(question.getAudio(), materialId, savedQuestionNode.getId(), partNumber, questionOrder + 1);
            questionOrder++;
        }
    }

    /**
     * Creates any missing placeholder question nodes needed to scaffold the expected tree structure.
     * These questions have no transcript text, config, or audio—they can be filled in later via PATCH.
     */
    private void createMissingPlaceholderQuestions(Long materialId, Long partNodeId, int existingQuestionCount, int expectedQuestionCount) {
        Instant now = Instant.now();
        for (int i = existingQuestionCount; i < expectedQuestionCount; i++) {
            String title = "Question " + (i + 1);
            MaterialNode questionNode = MaterialNode.builder()
                    .id(null)
                    .materialId(materialId)
                    .parentNodeId(partNodeId)
                    .kind("ITEM")
                    .title(title)
                    .displayOrder(i)
                    .skillId(TOEFL_SKILL_ID)
                    .transcriptText(null)  // placeholder: empty
                    .responseMode("SPOKEN")
                    .responseRequired(true)
                    .scoringMode("NONE")
                    .config(new HashMap<>())  // placeholder: empty config
                    .version(0L)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            materialNodeRepository.save(questionNode);
        }
    }

    private int safeSize(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private List<Long> collectSubtreeNodeIds(Long rootNodeId) {
        List<Long> ids = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> toVisit = new ArrayDeque<>();
        toVisit.add(rootNodeId);

        while (!toVisit.isEmpty()) {
            Long currentId = toVisit.removeFirst();
            if (!visited.add(currentId)) {
                continue;
            }
            ids.add(currentId);

            List<MaterialNode> children = materialNodeRepository.findByParentNodeId(currentId);
            for (MaterialNode child : children) {
                if (child.getId() != null) {
                    toVisit.addLast(child.getId());
                }
            }
        }

        return ids;
    }

    @Override
    public void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command) {
        if (command == null || command.getMaterialId() == null) {
            throw new IllegalArgumentException("materialId is required");
        }

        // Track newly uploaded storage keys so we can delete them on failure (compensation).
        List<String> uploadedKeys = new ArrayList<>();
        Set<String> storageKeysBeforeUpdate = Set.of();
        Set<String> storageKeysAfterUpdate;
        Material material;
        MaterialNode rootNode;
        boolean materialDirty = false;
        boolean titlesChanged = false;

        try {
            // ── Load material and root section node ──────────────────────────────
            material = materialRepository.findById(command.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + command.getMaterialId()));
            rootNode = materialNodeRepository.findById(material.getMaterialNodeId())
                    .orElseThrow(() -> new IllegalArgumentException("Root section node not found for material: " + command.getMaterialId()));
            storageKeysBeforeUpdate = collectStorageKeysForSubtree(rootNode.getId());

            // ── Update material text fields ──────────────────────────────────────
            if (hasText(command.getMaterialTitle()) && !Objects.equals(material.getTitle(), command.getMaterialTitle())) {
                material.setTitle(command.getMaterialTitle());
                rootNode.setTitle(command.getMaterialTitle());
                titlesChanged = true;
                materialDirty = true;
            }
            if (command.getMaterialDescription() != null) {
                material.setDescription(command.getMaterialDescription());
                materialDirty = true;
            }
            if (materialDirty) {
                Instant now = Instant.now();
                material.setUpdatedAt(now);
                material.setVersion(material.getVersion() + 1);
                materialRepository.save(material);
                rootNode.setUpdatedAt(now);
                rootNode.setVersion(rootNode.getVersion() + 1);
                materialNodeRepository.save(rootNode);
            }

            // ── Part 1 ───────────────────────────────────────────────────────────
            MaterialNode part1 = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 0)
                    .orElseThrow(() -> new IllegalArgumentException("Part 1 node not found for section: " + command.getMaterialId()));

            if (hasText(command.getPartTitle()) && !Objects.equals(part1.getTitle(), command.getPartTitle())) {
                part1.setTitle(command.getPartTitle());
                titlesChanged = true;
                part1.setUpdatedAt(Instant.now());
                part1.setVersion(part1.getVersion() + 1);
                materialNodeRepository.save(part1);
            }

            if (isFilePresent(command.getPartImage())) {
                if (Boolean.TRUE.equals(command.getRemovePartImage())) {
                    throw new IllegalArgumentException("partImage and removePartImage cannot both be set");
                }
                replaceAsset(command.getPartImage(), command.getMaterialId(), part1.getId(),
                        MaterialAsset.Kind.IMAGE, 1, null, uploadedKeys);
            } else if (Boolean.TRUE.equals(command.getRemovePartImage())) {
                deleteAsset(part1.getId(), MaterialAsset.Kind.IMAGE);
            }

            if (command.getQuestions() != null) {
                for (SpeakingQuestionPartialUpdateCommand q : command.getQuestions()) {
                    if (isEmptyQuestionUpdate(q)) continue;
                    updateQuestionNode(q, command.getMaterialId(), part1.getId(), 1, uploadedKeys);
                }
            }

            // ── Part 2 (only if any part-2 update was requested) ─────────────────
            boolean part2Requested = hasText(command.getPart2Title())
                    || (command.getPart2Questions() != null
                        && command.getPart2Questions().stream().anyMatch(q -> !isEmptyQuestionUpdate(q)));

            if (part2Requested) {
                MaterialNode part2 = materialNodeRepository.findByParentIdAndDisplayOrder(rootNode.getId(), 1)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Part 2 node not found for section: " + command.getMaterialId()
                                + ". Cannot update a part that does not exist."));

                if (hasText(command.getPart2Title()) && !Objects.equals(part2.getTitle(), command.getPart2Title())) {
                    part2.setTitle(command.getPart2Title());
                    titlesChanged = true;
                    part2.setUpdatedAt(Instant.now());
                    part2.setVersion(part2.getVersion() + 1);
                    materialNodeRepository.save(part2);
                }

                if (command.getPart2Questions() != null) {
                    for (SpeakingQuestionPartialUpdateCommand q : command.getPart2Questions()) {
                        if (isEmptyQuestionUpdate(q)) continue;
                        updateQuestionNode(q, command.getMaterialId(), part2.getId(), 2, uploadedKeys);
                    }
                }
            }

            storageKeysAfterUpdate = collectStorageKeysForSubtree(rootNode.getId());

        } catch (Exception e) {
            // Compensation: delete any files that were successfully uploaded before the failure.
            Set<String> keysToDeleteOnFailure = new HashSet<>(uploadedKeys);
            keysToDeleteOnFailure.removeAll(storageKeysBeforeUpdate);
            for (String key : keysToDeleteOnFailure) {
                try {
                    storageRepositoryPort.deleteObject("toefl", key);
                } catch (Exception ex) {
                    // Log and continue – a stale orphan in storage is preferable to masking the root cause.
                }
            }
            throw e;
        }

        // Post-success: best-effort removal of storage keys no longer referenced by this section.
        Set<String> keysToDeleteAfterSuccess = new HashSet<>(storageKeysBeforeUpdate);
        keysToDeleteAfterSuccess.removeAll(storageKeysAfterUpdate);
        for (String key : keysToDeleteAfterSuccess) {
            try {
                storageRepositoryPort.deleteObject("toefl", key);
            } catch (Exception ex) {
                // Intentionally swallowed – do NOT roll back the DB transaction over a stale object.
            }
        }

        if (titlesChanged && !materialDirty) {
            Instant now = Instant.now();
            material.setUpdatedAt(now);
            material.setVersion(material.getVersion() + 1);
            materialRepository.save(material);
        }

        if (titlesChanged) {

            MaterialDetailsUpsertedEvent event = MaterialDetailsUpsertedEvent.builder()
                    .materialId(command.getMaterialId())
                    .version(material.getVersion())
                    .materialTitle(material.getTitle())
                    .part1Title(resolvePartTitle(rootNode.getId(), 0))
                    .part2Title(resolvePartTitle(rootNode.getId(), 1))
                    .description(material.getDescription())
                    .updatedAt(Instant.now())
                    .build();

            outboxPort.append(
                    UUID.randomUUID(),
                    "Material",
                    material.getId().toString(),
                    MATERIAL_DETAILS_UPSERTED,
                    event
            );
        }
    }

    private String resolvePartTitle(Long rootNodeId, int displayOrder) {
        return materialNodeRepository.findByParentIdAndDisplayOrder(rootNodeId, displayOrder)
                .map(MaterialNode::getTitle)
                .orElse(null);
    }

    /**
     * Applies a partial update command to a single question (ITEM) node.
     * Uploads a new audio file if provided and records the replaced key for later cleanup.
     */
    private void updateQuestionNode(
            SpeakingQuestionPartialUpdateCommand q,
            Long materialId,
            Long partNodeId,
            int partNumber,
            List<String> uploadedKeys) {

        MaterialNode questionNode = materialNodeRepository.findByParentIdAndDisplayOrder(partNodeId, q.getIndex())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question at index " + q.getIndex() + " not found under part node " + partNodeId));

        boolean dirty = false;
        if (hasText(q.getTranscriptText())) {
            questionNode.setTranscriptText(q.getTranscriptText());
            dirty = true;
        }
        if (q.getConfig() != null) {
            questionNode.setConfig(q.getConfig());
            dirty = true;
        }
        if (dirty) {
            questionNode.setUpdatedAt(Instant.now());
            questionNode.setVersion(questionNode.getVersion() + 1);
            materialNodeRepository.save(questionNode);
        }

        if (isFilePresent(q.getAudio())) {
            if (Boolean.TRUE.equals(q.getRemoveAudio())) {
                throw new IllegalArgumentException("audio and removeAudio cannot both be set for question index " + q.getIndex());
            }
            replaceAsset(q.getAudio(), materialId, questionNode.getId(),
                    MaterialAsset.Kind.AUDIO, partNumber, questionNode.getDisplayOrder() + 1, uploadedKeys);
        } else if (Boolean.TRUE.equals(q.getRemoveAudio())) {
            deleteAsset(questionNode.getId(), MaterialAsset.Kind.AUDIO);
        }
    }

    /**
     * Uploads {@code file} to storage and updates (or creates) the matching {@link MaterialAsset}
     * DB record for {@code materialNodeId}/{@code kind}.
     */
    private void replaceAsset(
            UploadedFileCommand file,
            Long materialId,
            Long materialNodeId,
            MaterialAsset.Kind kind,
            int partNumber,
            Integer questionNumber,
            List<String> uploadedKeys) {

        String originalFilename = hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String newKey = buildSpeakingStorageKey(materialId, partNumber, kind, questionNumber);
        storageRepositoryPort.uploadObject("toefl", newKey, new ByteArrayInputStream(file.getBytes()));
        uploadedKeys.add(newKey);

        List<MaterialAsset> existing = materialAssetRepository.findByMaterialNodeId(materialNodeId);
        MaterialAsset asset = existing.stream()
                .filter(a -> a.getKind() == kind)
                .findFirst()
                .orElse(null);

        if (asset != null) {
            asset.setStorageKey(newKey);
            asset.setOriginalFilename(originalFilename);
            asset.setMimeType(file.getContentType());
            asset.setFileSizeBytes(file.getSize());
            asset.setVersion(asset.getVersion() + 1);
            asset.setUpdatedAt(OffsetDateTime.now());
        } else {
            asset = new MaterialAsset();
            asset.setMaterialNodeId(materialNodeId);
            asset.setKind(kind);
            asset.setStorageKey(newKey);
            asset.setOriginalFilename(originalFilename);
            asset.setMimeType(file.getContentType());
            asset.setFileSizeBytes(file.getSize());
            asset.setDisplayOrder(0);
            asset.setVersion(0L);
            asset.setMetadata(new HashMap<>());
            OffsetDateTime now = OffsetDateTime.now();
            asset.setCreatedAt(now);
            asset.setUpdatedAt(now);
        }
        materialAssetRepository.save(asset);
    }

    private String buildSpeakingStorageKey(Long materialId, int partNumber, MaterialAsset.Kind kind, Integer questionNumber) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required for speaking storage keys");
        }
        String basePath = "speaking/" + materialId + "/part" + partNumber;
        if (kind == MaterialAsset.Kind.IMAGE) {
            return basePath + "/image/image.png";
        }
        if (kind == MaterialAsset.Kind.AUDIO) {
            if (questionNumber == null) {
                throw new IllegalArgumentException("questionNumber is required for speaking audio keys");
            }
            return basePath + "/audio/question_" + questionNumber + ".mp3";
        }
        throw new IllegalArgumentException("Unsupported speaking asset kind: " + kind);
    }

    private Set<String> collectStorageKeysForSubtree(Long rootNodeId) {
        Set<String> keys = new HashSet<>();
        for (Long nodeId : collectSubtreeNodeIds(rootNodeId)) {
            for (MaterialAsset asset : materialAssetRepository.findByMaterialNodeId(nodeId)) {
                if (hasText(asset.getStorageKey())) {
                    keys.add(asset.getStorageKey());
                }
            }
        }
        return keys;
    }

    private void deleteAsset(Long materialNodeId, MaterialAsset.Kind kind) {
        for (MaterialAsset asset : materialAssetRepository.findByMaterialNodeId(materialNodeId)) {
            if (asset.getKind() == kind && asset.getId() != null) {
                materialAssetRepository.deleteById(asset.getId());
            }
        }
    }

    private boolean isFilePresent(UploadedFileCommand file) {
        return file != null && file.getBytes() != null && file.getBytes().length > 0;
    }

    private boolean isEmptyQuestionUpdate(SpeakingQuestionPartialUpdateCommand q) {
        if (q == null) return true;
        return !hasText(q.getTranscriptText())
                && q.getConfig() == null
                && !isFilePresent(q.getAudio())
                && !Boolean.TRUE.equals(q.getRemoveAudio());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
