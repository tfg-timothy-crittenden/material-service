package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageCleanupPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.domain.model.*;
import com.timcritt.tfg.domain.policy.toefl.ToeflSpeaking2026MaterialPolicy;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED;
import static com.timcritt.tfg.application.integration.IntegrationEventTypes.MATERIAL_DELETED;

public class TOEFLSpeakingMaterialCommandService implements TOEFLSpeakingMaterialCommandUseCase {
    private static final Long TOEFL_SKILL_ID = 4L;
    private static final Long TOEFL_EXAM_FAMILY_ID = 1L;
    private static final int PART_1_QUESTION_COUNT = 7;
    private static final int PART_2_QUESTION_COUNT = 4;

    private final MaterialRepositoryPort materialRepository;
    private final StorageRepositoryPort storageRepositoryPort;
    private final StorageCleanupPort storageCleanupPort;
    private final IntegrationEventOutboxPort outboxPort;
    private final Supplier<UUID> replacementKeyUuidSupplier;

    public TOEFLSpeakingMaterialCommandService(
            MaterialRepositoryPort materialRepository,
            StorageRepositoryPort storageRepositoryPort,
            StorageCleanupPort storageCleanupPort,
            IntegrationEventOutboxPort outboxPort,
            Supplier<UUID> replacementKeyUuidSupplier) {
        this.materialRepository = materialRepository;
        this.storageRepositoryPort = storageRepositoryPort;
        this.storageCleanupPort = storageCleanupPort;
        this.outboxPort = outboxPort;
        this.replacementKeyUuidSupplier = replacementKeyUuidSupplier;
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

        Material material = buildMaterial(effectiveTitle, command.getMaterialDescription());
        MaterialNode rootNode = buildSectionRoot(effectiveTitle);
        material.attachRoot(rootNode);

        MaterialNode part1Node = buildPartNode(command.getPartTitle(), 0);
        rootNode.addChild(part1Node);
        addQuestions(part1Node, command.getQuestions(), PART_1_QUESTION_COUNT);

        MaterialNode part2Node = buildPartNode(command.getPart2Title(), 1);
        rootNode.addChild(part2Node);
        addQuestions(part2Node, command.getPart2Questions(), PART_2_QUESTION_COUNT);

        Material persisted = materialRepository.save(material);

        MaterialNode persistedRoot = persisted.getRoot();
        MaterialNode persistedPart1 = persistedRoot.childAt(0);
        MaterialNode persistedPart2 = persistedRoot.childAt(1);

        boolean assetsAttached = false;
        assetsAttached |= attachImageAsset(persisted, command.getPartImage(), persistedPart1.getId());
        assetsAttached |= attachQuestionAudioAssets(persisted, command.getQuestions(), persistedPart1, 1);
        assetsAttached |= attachQuestionAudioAssets(persisted, command.getPart2Questions(), persistedPart2, 2);

        if (!assetsAttached) {
            return persisted.getId();
        }

        Material finalMaterial = materialRepository.save(persisted);
        return finalMaterial.getId();
    }

    @Override
    public void publishSpeakingSection(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Material not found: " + materialId
                        )
                );

        material.publish(new ToeflSpeaking2026MaterialPolicy());

        materialRepository.save(material);
    }

    @Override
    public void deleteSpeakingSection(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required");
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId));

        Long rootNodeId = material.getRootId();
        Set<String> storageKeys = collectStorageKeys(material.getRoot());

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

        for (String key : storageKeys) {
            storageCleanupPort.requestDeletion(materialId, "toefl", key);
        }
    }

    private MaterialNode buildSectionRoot(String sectionTitle) {
        Instant now = Instant.now();
        return MaterialNode.builder()
                .id(null)
                .materialId(null)
                .parentNodeId(null)
                .kind(MaterialNodeKind.SECTION)
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
    }

    private Material buildMaterial(String materialTitle, String materialDescription) {
        Instant now = Instant.now();
        return Material.builder()
                .id(null)
                .examFamilyId(TOEFL_EXAM_FAMILY_ID)
                .title(materialTitle)
                .description(materialDescription)
                .authorId(null)
                .ownerOrgId(null)
                .status(MaterialStatus.DRAFT)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private MaterialNode buildPartNode(String title, int displayOrder) {
        Instant now = Instant.now();
        return MaterialNode.builder()
                .id(null)
                .materialId(null)
                .parentNodeId(null)
                .kind(MaterialNodeKind.PART)
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
    }

    private boolean attachImageAsset(Material material, UploadedFileCommand image, Long materialNodeId) {
        return attachAsset(material, image, materialNodeId, MaterialAsset.Kind.IMAGE,
                buildSpeakingStorageKey(material.getId(), 1, MaterialAsset.Kind.IMAGE, null));
    }

    private boolean attachAudioAsset(Material material, UploadedFileCommand audio, Long materialNodeId, int partNumber, int questionNumber) {
        return attachAsset(material, audio, materialNodeId, MaterialAsset.Kind.AUDIO,
                buildSpeakingStorageKey(material.getId(), partNumber, MaterialAsset.Kind.AUDIO, questionNumber));
    }

    private boolean attachAsset(
            Material material,
            UploadedFileCommand file,
            Long materialNodeId,
            MaterialAsset.Kind kind,
            String storageKey
    ) {
        if (file == null || file.getBytes() == null || file.getBytes().length == 0) {
            return false;
        }

        try {
            storageRepositoryPort.uploadObject("toefl", storageKey, new ByteArrayInputStream(file.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to storage", e);
        }

        String originalFilename = hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";

        material.addNodeAsset(
                materialNodeId,
                kind,
                storageKey,
                originalFilename,
                file.getContentType(),
                file.getSize()
        );
        return true;
    }

    private void addQuestions(MaterialNode partNode, List<SpeakingQuestionUploadCommand> questions, int expectedQuestionCount) {
        List<SpeakingQuestionUploadCommand> safeQuestions = questions == null ? Collections.emptyList() : new ArrayList<>(questions);

        int questionOrder = 0;
        for (SpeakingQuestionUploadCommand question : safeQuestions) {
            partNode.addChild(buildQuestionNode(questionOrder, question.getTranscriptText(), question.getConfig()));
            questionOrder++;
        }

        addMissingPlaceholderQuestions(partNode, safeQuestions.size(), expectedQuestionCount);
    }

    /**
     * Creates any missing placeholder question nodes needed to scaffold the expected tree structure.
     * These questions have no transcript text, config, or audio—they can be filled in later via PATCH.
     */
    private void addMissingPlaceholderQuestions(MaterialNode partNode, int existingQuestionCount, int expectedQuestionCount) {
        for (int i = existingQuestionCount; i < expectedQuestionCount; i++) {
            partNode.addChild(buildQuestionNode(i, null, new HashMap<>()));
        }
    }

    private MaterialNode buildQuestionNode(int displayOrder, String transcriptText, Map<String, Object> config) {
        Instant now = Instant.now();
        return MaterialNode.builder()
                .id(null)
                .materialId(null)
                .parentNodeId(null)
                .kind(MaterialNodeKind.ITEM)
                .title("Question " + (displayOrder + 1))
                .displayOrder(displayOrder)
                .skillId(TOEFL_SKILL_ID)
                .transcriptText(transcriptText)
                .responseMode("SPOKEN")
                .responseRequired(true)
                .scoringMode("NONE")
                .config(config == null ? new HashMap<>() : config)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private boolean attachQuestionAudioAssets(
            Material material,
            List<SpeakingQuestionUploadCommand> questions,
            MaterialNode persistedPartNode,
            int partNumber
    ) {
        List<SpeakingQuestionUploadCommand> safeQuestions = questions == null ? Collections.emptyList() : new ArrayList<>(questions);
        boolean assetsAttached = false;

        for (int questionOrder = 0; questionOrder < safeQuestions.size(); questionOrder++) {
            MaterialNode persistedQuestionNode = persistedPartNode.childAt(questionOrder);
            assetsAttached |= attachAudioAsset(
                    material,
                    safeQuestions.get(questionOrder).getAudio(),
                    persistedQuestionNode.getId(),
                    partNumber,
                    questionOrder + 1
            );
        }

        return assetsAttached;
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
        boolean materialDetailsChanged = false;
        boolean titlesChanged = false;
        boolean questionsChanged = false;

        try {
            // ── Load material and root section node ──────────────────────────────
            material = materialRepository.findById(command.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + command.getMaterialId()));
            if (!material.hasRoot()) {
                throw new IllegalArgumentException("Root section node not found for material: " + command.getMaterialId());
            }
            rootNode = material.getRoot();
            storageKeysBeforeUpdate = collectStorageKeys(rootNode);

            // ── Update material text fields ──────────────────────────────────────
            String suppliedTitle = hasText(command.getMaterialTitle()) ? command.getMaterialTitle() : null;
            if (suppliedTitle != null || command.getMaterialDescription() != null) {
                Long previousVersion = material.getVersion();
                String previousMaterialTitle = material.getTitle();
                String previousRootTitle = rootNode.getTitle();

                material.updateDetails(suppliedTitle, command.getMaterialDescription());

                materialDetailsChanged = !Objects.equals(previousVersion, material.getVersion());
                titlesChanged = !Objects.equals(previousMaterialTitle, material.getTitle())
                        || !Objects.equals(previousRootTitle, rootNode.getTitle());
            }

            // ── Part 1 ───────────────────────────────────────────────────────────
            MaterialNode part1 = rootNode.childAt(0);

            if (hasText(command.getPartTitle())) {
                Long previousVersion = material.getVersion();
                material.updateNodeTitle(part1.getId(), command.getPartTitle());
                titlesChanged |= !Objects.equals(previousVersion, material.getVersion());
            }

            if (isFilePresent(command.getPartImage())) {
                if (Boolean.TRUE.equals(command.getRemovePartImage())) {
                    throw new IllegalArgumentException("partImage and removePartImage cannot both be set");
                }
                Long previousVersion = material.getVersion();
                replaceAsset(command.getPartImage(), material, part1,
                        MaterialAsset.Kind.IMAGE, 1, null, uploadedKeys);
                questionsChanged |= !Objects.equals(previousVersion, material.getVersion());
            } else if (Boolean.TRUE.equals(command.getRemovePartImage())) {
                Long previousVersion = material.getVersion();
                deleteAssets(material, part1, MaterialAsset.Kind.IMAGE);
                questionsChanged |= !Objects.equals(previousVersion, material.getVersion());
            }

            if (command.getQuestions() != null) {
                for (SpeakingQuestionPartialUpdateCommand q : command.getQuestions()) {
                    if (isEmptyQuestionUpdate(q)) continue;
                    questionsChanged |= updateQuestionNode(q, material, part1.childAt(q.getIndex()), 1, uploadedKeys);
                }
            }

            // ── Part 2 (only if any part-2 update was requested) ─────────────────
            boolean part2Requested = hasText(command.getPart2Title())
                    || (command.getPart2Questions() != null
                        && command.getPart2Questions().stream().anyMatch(q -> !isEmptyQuestionUpdate(q)));

            if (part2Requested) {
                MaterialNode part2 = rootNode.childAt(1);

                if (hasText(command.getPart2Title())) {
                    Long previousVersion = material.getVersion();
                    material.updateNodeTitle(part2.getId(), command.getPart2Title());
                    titlesChanged |= !Objects.equals(previousVersion, material.getVersion());
                }

                if (command.getPart2Questions() != null) {
                    for (SpeakingQuestionPartialUpdateCommand q : command.getPart2Questions()) {
                        if (isEmptyQuestionUpdate(q)) continue;
                        questionsChanged |= updateQuestionNode(q, material, part2.childAt(q.getIndex()), 2, uploadedKeys);
                    }
                }
            }

            storageKeysAfterUpdate = collectStorageKeys(rootNode);

            // Persist once, after all question and title mutations are in the aggregate.
            if (materialDetailsChanged || titlesChanged || questionsChanged) {
                materialRepository.save(material);
            }

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

        // Post-success: defer destructive cleanup of now-obsolete keys until after commit.
        Set<String> keysToDeleteAfterSuccess = new HashSet<>(storageKeysBeforeUpdate);
        keysToDeleteAfterSuccess.removeAll(storageKeysAfterUpdate);
        for (String key : keysToDeleteAfterSuccess) {
            storageCleanupPort.requestDeletion(material.getId(), "toefl", key);
        }

        if (titlesChanged) {

            MaterialDetailsUpsertedEvent event = MaterialDetailsUpsertedEvent.builder()
                    .materialId(command.getMaterialId())
                    .version(material.getVersion())
                    .materialTitle(material.getTitle())
                    .part1Title(rootNode.childAt(0).getTitle())
                    .part2Title(rootNode.childAt(1).getTitle())
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


    /**
     * Applies a partial update through the owning Material and reports aggregate-state changes.
     * Uploads a new audio file if provided and records the replaced key for later cleanup.
     */
    private boolean updateQuestionNode(
            SpeakingQuestionPartialUpdateCommand q,
            Material material,
            MaterialNode questionNode,
            int partNumber,
            List<String> uploadedKeys) {

        Long previousVersion = material.getVersion();
        if (hasText(q.getTranscriptText())) {
            material.updateNodeTranscript(questionNode.getId(), q.getTranscriptText());
        }
        if (q.getConfig() != null) {
            material.updateNodeConfig(questionNode.getId(), q.getConfig());
        }
        if (isFilePresent(q.getAudio())) {
            if (Boolean.TRUE.equals(q.getRemoveAudio())) {
                throw new IllegalArgumentException("audio and removeAudio cannot both be set for question index " + q.getIndex());
            }
            replaceAsset(q.getAudio(), material, questionNode,
                    MaterialAsset.Kind.AUDIO, partNumber, questionNode.getDisplayOrder() + 1, uploadedKeys);
        } else if (Boolean.TRUE.equals(q.getRemoveAudio())) {
            deleteAssets(material, questionNode, MaterialAsset.Kind.AUDIO);
        }
        return !Objects.equals(previousVersion, material.getVersion());
    }

    /**
     * Uploads {@code file} to storage and updates (or creates) the first matching attached
     * {@link MaterialAsset} of {@code kind} under {@code node}.
     */
    private void replaceAsset(
            UploadedFileCommand file,
            Material material,
            MaterialNode node,
            MaterialAsset.Kind kind,
            int partNumber,
            Integer questionNumber,
            List<String> uploadedKeys
    ) {
        String originalFilename = hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "file";

        MaterialAsset asset = node.getAssets().stream()
                .filter(existing -> existing.getKind() == kind)
                .findFirst()
                .orElse(null);

        String newKey = asset == null
                ? buildSpeakingStorageKey(material.getId(), partNumber, kind, questionNumber)
                : buildSpeakingReplacementStorageKey(material.getId(), partNumber, kind, questionNumber, file);

        storageRepositoryPort.uploadObject(
                "toefl",
                newKey,
                new ByteArrayInputStream(file.getBytes())
        );

        uploadedKeys.add(newKey);

        if (asset != null) {
            material.replaceNodeAssetFile(
                    node.getId(),
                    asset.getId(),
                    newKey,
                    originalFilename,
                    file.getContentType(),
                    file.getSize()
            );
        } else {
            material.addNodeAsset(
                    node.getId(),
                    kind,
                    newKey,
                    originalFilename,
                    file.getContentType(),
                    file.getSize()
            );
        }
    }

    private String buildSpeakingReplacementStorageKey(
            Long materialId,
            int partNumber,
            MaterialAsset.Kind kind,
            Integer questionNumber,
            UploadedFileCommand file
    ) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required for speaking storage keys");
        }

        UUID replacementId = Objects.requireNonNull(
                replacementKeyUuidSupplier.get(),
                "replacementKeyUuidSupplier returned null"
        );
        String extension = extractStorageExtension(file == null ? null : file.getOriginalFilename(), kind);
        String basePath = "speaking/" + materialId + "/part" + partNumber;

        if (kind == MaterialAsset.Kind.IMAGE) {
            return basePath + "/image/" + replacementId + "." + extension;
        }
        if (kind == MaterialAsset.Kind.AUDIO) {
            if (questionNumber == null) {
                throw new IllegalArgumentException("questionNumber is required for speaking audio keys");
            }
            return basePath + "/audio/question_" + questionNumber + "/" + replacementId + "." + extension;
        }
        throw new IllegalArgumentException("Unsupported speaking asset kind: " + kind);
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

    private String extractStorageExtension(String originalFilename, MaterialAsset.Kind kind) {
        if (!hasText(originalFilename)) {
            return defaultStorageExtension(kind);
        }

        String normalizedFilename = originalFilename.replace('\\', '/');
        int lastSeparator = normalizedFilename.lastIndexOf('/');
        String leafName = lastSeparator >= 0
                ? normalizedFilename.substring(lastSeparator + 1)
                : normalizedFilename;
        int lastDot = leafName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == leafName.length() - 1) {
            return defaultStorageExtension(kind);
        }

        String extension = leafName.substring(lastDot + 1).trim().toLowerCase(Locale.ROOT);
        if (extension.isEmpty() || !extension.matches("[a-z0-9]+")) {
            return defaultStorageExtension(kind);
        }
        return extension;
    }

    private String defaultStorageExtension(MaterialAsset.Kind kind) {
        if (kind == MaterialAsset.Kind.IMAGE) {
            return "png";
        }
        if (kind == MaterialAsset.Kind.AUDIO) {
            return "mp3";
        }
        throw new IllegalArgumentException("Unsupported speaking asset kind: " + kind);
    }

    private Set<String> collectStorageKeys(MaterialNode rootNode) {
        Set<String> keys = new LinkedHashSet<>();
        if (rootNode == null) {
            return keys;
        }

        ArrayDeque<MaterialNode> toVisit = new ArrayDeque<>();
        toVisit.add(rootNode);

        while (!toVisit.isEmpty()) {
            MaterialNode node = toVisit.removeFirst();
            for (MaterialAsset asset : node.getAssets()) {
                if (hasText(asset.getStorageKey())) {
                    keys.add(asset.getStorageKey());
                }
            }
            toVisit.addAll(node.getChildren());
        }
        return keys;
    }

    private void deleteAssets(Material material, MaterialNode node, MaterialAsset.Kind kind) {
        List<Long> assetIdsToRemove = node.getAssets().stream()
                .filter(asset -> asset.getKind() == kind)
                .map(MaterialAsset::getId)
                .filter(Objects::nonNull)
                .toList();

        for (Long assetId : assetIdsToRemove) {
            material.removeNodeAsset(node.getId(), assetId);
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
