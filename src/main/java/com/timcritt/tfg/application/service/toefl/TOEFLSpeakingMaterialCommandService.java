package com.timcritt.tfg.application.service.toefl;

import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingPart1UploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TOEFLSpeakingMaterialCommandService implements TOEFLSpeakingMaterialCommandUseCase {
    private static final Long TOEFL_SKILL_ID = 4L;
    private static final Long TOEFL_EXAM_FAMILY_ID = 1L;

    private final MaterialRepositoryPort materialRepository;
    private final MaterialNodeRepositoryPort materialNodeRepository;
    private final MaterialAssetRepositoryPort materialAssetRepository;
    private final StorageRepositoryPort storageRepositoryPort;

    public TOEFLSpeakingMaterialCommandService(
            MaterialRepositoryPort materialRepository,
            MaterialNodeRepositoryPort materialNodeRepository,
            MaterialAssetRepositoryPort materialAssetRepository,
            StorageRepositoryPort storageRepositoryPort) {
        this.materialRepository = materialRepository;
        this.materialNodeRepository = materialNodeRepository;
        this.materialAssetRepository = materialAssetRepository;
        this.storageRepositoryPort = storageRepositoryPort;
    }

    @Override
    public void uploadSpeakingPart1(TOEFLSpeakingPart1UploadCommand command) {
        validatePart1(command);

        MaterialNode savedRootNode = createSectionRoot(command.getMaterialTitle());
        createMaterial(command.getMaterialTitle(), command.getMaterialDescription(), savedRootNode.getId());

        MaterialNode part1Node = createPartNode(savedRootNode.getId(), command.getPartTitle(), 0);
        saveImageAsset(command.getPartImage(), part1Node.getId(), "part1/image");
        createQuestions(part1Node.getId(), command.getQuestions(), "part1/audio");
    }

    @Override
    public void uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command) {
        validatePart1Section(command);
        validatePart2(command);

        MaterialNode savedRootNode = createSectionRoot(command.getMaterialTitle());
        createMaterial(command.getMaterialTitle(), command.getMaterialDescription(), savedRootNode.getId());

        MaterialNode part1Node = createPartNode(savedRootNode.getId(), command.getPartTitle(), 0);
        saveImageAsset(command.getPartImage(), part1Node.getId(), "part1/image");
        createQuestions(part1Node.getId(), command.getQuestions(), "part1/audio");

        MaterialNode part2Node = createPartNode(savedRootNode.getId(), command.getPart2Title(), 1);
        createQuestions(part2Node.getId(), command.getPart2Questions(), "part2/audio");
    }

    private void validatePart1(TOEFLSpeakingPart1UploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload is required");
        }
        if (!hasText(command.getMaterialTitle())) {
            throw new IllegalArgumentException("materialTitle is required");
        }
        if (!hasText(command.getPartTitle())) {
            throw new IllegalArgumentException("partTitle is required");
        }
        if (command.getQuestions() == null || command.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("questions are required");
        }
        if (command.getPartImage() == null || command.getPartImage().getBytes() == null || command.getPartImage().getBytes().length == 0) {
            throw new IllegalArgumentException("partImage is required and must not be empty");
        }
        validateQuestions(command.getQuestions(), "questions", true);
    }

    private void validatePart1Section(TOEFLSpeakingSectionUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("payload is required");
        }
        if (!hasText(command.getMaterialTitle())) {
            throw new IllegalArgumentException("materialTitle is required");
        }
        if (!hasText(command.getPartTitle())) {
            throw new IllegalArgumentException("partTitle is required");
        }
        if (command.getQuestions() == null || command.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("questions are required");
        }
        if (command.getPartImage() == null || command.getPartImage().getBytes() == null || command.getPartImage().getBytes().length == 0) {
            throw new IllegalArgumentException("partImage is required and must not be empty");
        }
        validateQuestions(command.getQuestions(), "questions", true);
    }

    private void validatePart2(TOEFLSpeakingSectionUploadCommand command) {
        if (!hasText(command.getPart2Title())) {
            throw new IllegalArgumentException("part2Title is required");
        }
        if (command.getPart2Questions() == null || command.getPart2Questions().size() != 4) {
            throw new IllegalArgumentException("part2Questions must contain exactly 4 questions");
        }
        validateQuestions(command.getPart2Questions(), "part2Questions", false);
    }

    private void validateQuestions(List<SpeakingQuestionUploadCommand> questions, String fieldName, boolean requireConfig) {
        for (int i = 0; i < questions.size(); i++) {
            SpeakingQuestionUploadCommand question = questions.get(i);
            if (question == null) {
                throw new IllegalArgumentException(fieldName + "[" + i + "] is required");
            }
            if (!hasText(question.getTranscriptText())) {
                throw new IllegalArgumentException(fieldName + "[" + i + "].transcriptText is required");
            }
            UploadedFileCommand audio = question.getAudio();
            if (audio == null || audio.getBytes() == null || audio.getBytes().length == 0) {
                throw new IllegalArgumentException("Audio file is required for question index " + i);
            }
            if (requireConfig && question.getConfig() == null) {
                throw new IllegalArgumentException(fieldName + "[" + i + "].config is required");
            }
        }
    }

    private MaterialNode createSectionRoot(String sectionTitle) {
        Instant now = Instant.now();
        MaterialNode sectionNode = MaterialNode.builder()
                .id(null)
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

    private void createMaterial(String materialTitle, String materialDescription, Long rootNodeId) {
        Instant now = Instant.now();
        Material material = Material.builder()
                .id(null)
                .examFamilyId(TOEFL_EXAM_FAMILY_ID)
                .materialNodeId(rootNodeId)
                .title(materialTitle)
                .description(materialDescription)
                .authorId(null)
                .ownerOrgId(null)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        materialRepository.save(material);
    }

    private MaterialNode createPartNode(Long parentNodeId, String title, int displayOrder) {
        Instant now = Instant.now();
        MaterialNode partNode = MaterialNode.builder()
                .id(null)
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

    private void saveImageAsset(UploadedFileCommand image, Long materialNodeId, String keyPrefix) {
        saveAsset(image, materialNodeId, MaterialAsset.Kind.IMAGE, keyPrefix);
    }

    private void saveAudioAsset(UploadedFileCommand audio, Long materialNodeId, String keyPrefix) {
        saveAsset(audio, materialNodeId, MaterialAsset.Kind.AUDIO, keyPrefix);
    }

    private void saveAsset(UploadedFileCommand file, Long materialNodeId, MaterialAsset.Kind kind, String keyPrefix) {
        if (file == null || file.getBytes() == null || file.getBytes().length == 0) {
            return;
        }

        String originalFilename = hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String storageKey = keyPrefix + "/" + System.currentTimeMillis() + "_" + originalFilename;
        try {
            storageRepositoryPort.uploadObject("toefl", storageKey, new ByteArrayInputStream(file.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to storage", e);
        }

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

    private void createQuestions(Long partNodeId, List<SpeakingQuestionUploadCommand> questions, String audioPrefix) {
        Instant now = Instant.now();
        List<SpeakingQuestionUploadCommand> safeQuestions = questions == null ? Collections.emptyList() : new ArrayList<>(questions);

        int questionOrder = 0;
        for (SpeakingQuestionUploadCommand question : safeQuestions) {
            String title = "Question " + (questionOrder + 1);
            MaterialNode questionNode = MaterialNode.builder()
                    .id(null)
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
            saveAudioAsset(question.getAudio(), savedQuestionNode.getId(), audioPrefix);
            questionOrder++;
        }
    }

    @Override
    public void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command) {
        if (command == null || command.getMaterialId() == null) {
            throw new IllegalArgumentException("materialId is required");
        }

        // Track newly uploaded storage keys so we can delete them on failure (compensation).
        List<String> uploadedKeys = new ArrayList<>();
        // Track replaced (old) storage keys so we can delete them on success (best-effort cleanup).
        List<String> keysToDeleteAfterSuccess = new ArrayList<>();

        try {
            // ── Load material and root section node ──────────────────────────────
            Material material = materialRepository.findById(command.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + command.getMaterialId()));
            MaterialNode rootNode = materialNodeRepository.findById(material.getMaterialNodeId())
                    .orElseThrow(() -> new IllegalArgumentException("Root section node not found for material: " + command.getMaterialId()));

            // ── Update material text fields ──────────────────────────────────────
            boolean materialDirty = false;
            if (hasText(command.getMaterialTitle())) {
                material.setTitle(command.getMaterialTitle());
                rootNode.setTitle(command.getMaterialTitle());
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

            if (hasText(command.getPartTitle())) {
                part1.setTitle(command.getPartTitle());
                part1.setUpdatedAt(Instant.now());
                part1.setVersion(part1.getVersion() + 1);
                materialNodeRepository.save(part1);
            }

            if (isFilePresent(command.getPartImage())) {
                String oldKey = replaceAsset(command.getPartImage(), part1.getId(),
                        MaterialAsset.Kind.IMAGE, "part1/image", uploadedKeys);
                if (oldKey != null) keysToDeleteAfterSuccess.add(oldKey);
            }

            if (command.getQuestions() != null) {
                for (SpeakingQuestionPartialUpdateCommand q : command.getQuestions()) {
                    if (isEmptyQuestionUpdate(q)) continue;
                    updateQuestionNode(q, part1.getId(), "part1/audio", uploadedKeys, keysToDeleteAfterSuccess);
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

                if (hasText(command.getPart2Title())) {
                    part2.setTitle(command.getPart2Title());
                    part2.setUpdatedAt(Instant.now());
                    part2.setVersion(part2.getVersion() + 1);
                    materialNodeRepository.save(part2);
                }

                if (command.getPart2Questions() != null) {
                    for (SpeakingQuestionPartialUpdateCommand q : command.getPart2Questions()) {
                        if (isEmptyQuestionUpdate(q)) continue;
                        updateQuestionNode(q, part2.getId(), "part2/audio", uploadedKeys, keysToDeleteAfterSuccess);
                    }
                }
            }

        } catch (Exception e) {
            // Compensation: delete any files that were successfully uploaded before the failure.
            for (String key : uploadedKeys) {
                try {
                    storageRepositoryPort.deleteObject("toefl", key);
                } catch (Exception ex) {
                    // Log and continue – a stale orphan in storage is preferable to masking the root cause.
                }
            }
            throw e;
        }

        // Post-success: best-effort removal of the storage objects that were replaced.
        // If a delete fails, the old object becomes an orphan in the object store, which can be
        // reconciled by a periodic cleanup job comparing storage keys against the DB.
        for (String key : keysToDeleteAfterSuccess) {
            try {
                storageRepositoryPort.deleteObject("toefl", key);
            } catch (Exception ex) {
                // Intentionally swallowed – do NOT roll back the DB transaction over a stale object.
            }
        }
    }

    /**
     * Applies a partial update command to a single question (ITEM) node.
     * Uploads a new audio file if provided and records the replaced key for later cleanup.
     */
    private void updateQuestionNode(
            SpeakingQuestionPartialUpdateCommand q,
            Long partNodeId,
            String audioPrefix,
            List<String> uploadedKeys,
            List<String> keysToDeleteAfterSuccess) {

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
            String oldKey = replaceAsset(q.getAudio(), questionNode.getId(),
                    MaterialAsset.Kind.AUDIO, audioPrefix, uploadedKeys);
            if (oldKey != null) keysToDeleteAfterSuccess.add(oldKey);
        }
    }

    /**
     * Uploads {@code file} to storage and updates (or creates) the matching {@link MaterialAsset}
     * DB record for {@code materialNodeId}/{@code kind}.
     *
     * @return the old storage key if an asset was replaced, or {@code null} if a new one was created.
     */
    private String replaceAsset(
            UploadedFileCommand file,
            Long materialNodeId,
            MaterialAsset.Kind kind,
            String keyPrefix,
            List<String> uploadedKeys) {

        String originalFilename = hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String newKey = keyPrefix + "/" + System.currentTimeMillis() + "_" + originalFilename;
        storageRepositoryPort.uploadObject("toefl", newKey, new ByteArrayInputStream(file.getBytes()));
        uploadedKeys.add(newKey);

        List<MaterialAsset> existing = materialAssetRepository.findByMaterialNodeId(materialNodeId);
        MaterialAsset asset = existing.stream()
                .filter(a -> a.getKind() == kind)
                .findFirst()
                .orElse(null);

        String oldKey = null;
        if (asset != null) {
            oldKey = asset.getStorageKey();
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
        return oldKey;
    }

    private boolean isFilePresent(UploadedFileCommand file) {
        return file != null && file.getBytes() != null && file.getBytes().length > 0;
    }

    private boolean isEmptyQuestionUpdate(SpeakingQuestionPartialUpdateCommand q) {
        if (q == null) return true;
        return !hasText(q.getTranscriptText()) && q.getConfig() == null && !isFilePresent(q.getAudio());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
