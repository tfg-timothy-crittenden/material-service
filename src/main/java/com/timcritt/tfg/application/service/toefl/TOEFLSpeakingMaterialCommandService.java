package com.timcritt.tfg.application.service.toefl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingPart1UploadDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TOEFLSpeakingMaterialCommandService {
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

    @Transactional
    public void uploadSpeakingPart1(TOEFLSpeakingPart1UploadDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("payload is required");
        }
        if (!org.springframework.util.StringUtils.hasText(dto.getMaterialTitle())) {
            throw new IllegalArgumentException("materialTitle is required");
        }
        if (dto.getQuestions() == null || dto.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("questions are required");
        }

        // Validate part image is present
        if (dto.getPartImage() == null || dto.getPartImage().isEmpty()) {
            throw new IllegalArgumentException("partImage is required and must not be empty");
        }
        // Validate every question has audio
        for (int i = 0; i < dto.getQuestions().size(); i++) {
            TOEFLSpeakingPart1UploadDto.QuestionUpload q = dto.getQuestions().get(i);
            if (q.getAudio() == null || q.getAudio().isEmpty()) {
                throw new IllegalArgumentException("Audio file is required for question index " + i);
            }
        }

        Instant now = Instant.now();
        // Track uploaded object keys for cleanup on failure (future use)
        // List<String> uploadedObjectKeys = new ArrayList<>();
        MaterialNode rootNode = MaterialNode.builder()
                .id(null)
                .parentNodeId(null)
                .kind("SECTION")
                .title(dto.getMaterialTitle())
                .displayOrder(0)
                .skillId(4L)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        MaterialNode savedRootNode = materialNodeRepository.save(rootNode);

        Material material = Material.builder()
                .id(null)
                .examFamilyId(1L) // Set backend-controlled examFamilyId
                .materialNodeId(savedRootNode.getId())
                .title(dto.getMaterialTitle())
                .description(dto.getMaterialDescription())
                .authorId(null)
                .ownerOrgId(null)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        materialRepository.save(material);

        MaterialNode partNode = MaterialNode.builder()
                .id(null)
                .parentNodeId(savedRootNode.getId())
                .kind("PART")
                .title(dto.getPartTitle()) // Use frontend-provided part title
                .displayOrder(0) // Set backend-controlled displayOrder
                .skillId(4L)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        MaterialNode savedPartNode = materialNodeRepository.save(partNode);

        // Handle part image upload as MaterialAsset if present (associate only with PART node)
        MultipartFile partImage = dto.getPartImage();
        if (partImage != null && !partImage.isEmpty()) {
            String storageKey = "part1/image/" + System.currentTimeMillis() + "_" + partImage.getOriginalFilename();
            try {
                storageRepositoryPort.uploadObject("toefl", storageKey, partImage.getInputStream());
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload part image to storage", e);
            }
            MaterialAsset imageAsset = new MaterialAsset();
            imageAsset.setMaterialNodeId(savedPartNode.getId()); // PART node
            imageAsset.setKind(MaterialAsset.Kind.IMAGE);
            imageAsset.setStorageKey(storageKey);
            imageAsset.setOriginalFilename(partImage.getOriginalFilename());
            imageAsset.setMimeType(partImage.getContentType());
            imageAsset.setFileSizeBytes(partImage.getSize());
            imageAsset.setDisplayOrder(0);
            imageAsset.setVersion(0L);
            imageAsset.setMetadata(new HashMap<>());
            OffsetDateTime nowOffset = OffsetDateTime.now();
            imageAsset.setCreatedAt(nowOffset);
            imageAsset.setUpdatedAt(nowOffset);
            materialAssetRepository.save(imageAsset);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        int questionOrder = 0;
        for (TOEFLSpeakingPart1UploadDto.QuestionUpload q : dto.getQuestions()) {
            String title = "Question " + (questionOrder + 1);
            Map<String, Object> configMap;
            try {
                configMap = objectMapper.readValue(q.getConfig(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid config JSON for question index " + questionOrder, e);
            }
            MaterialNode questionNode = MaterialNode.builder()
                    .id(null)
                    .parentNodeId(savedPartNode.getId())
                    .kind("ITEM")
                    .title(title)
                    .displayOrder(questionOrder)
                    .skillId(4L)
                    .transcriptText(q.getTranscriptText())
                    .responseMode("SPOKEN")
                    .responseRequired(true)
                    .scoringMode("NONE")
                    .config(configMap)
                    .version(0L)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            MaterialNode savedQuestionNode = materialNodeRepository.save(questionNode);

            // Save audio as MaterialAsset if provided (associate with ITEM node)
            MultipartFile audio = q.getAudio();
            if (audio != null && !audio.isEmpty()) {
                String audioStorageKey = "part1/audio/" + System.currentTimeMillis() + "_" + audio.getOriginalFilename();
                try {
                    storageRepositoryPort.uploadObject("toefl", audioStorageKey, audio.getInputStream());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload question audio to storage", e);
                }
                MaterialAsset audioAsset = new MaterialAsset();
                audioAsset.setMaterialNodeId(savedQuestionNode.getId()); // ITEM node
                audioAsset.setKind(MaterialAsset.Kind.AUDIO);
                audioAsset.setStorageKey(audioStorageKey);
                audioAsset.setOriginalFilename(audio.getOriginalFilename());
                audioAsset.setMimeType(audio.getContentType());
                audioAsset.setFileSizeBytes(audio.getSize());
                audioAsset.setDisplayOrder(0);
                audioAsset.setVersion(0L);
                audioAsset.setMetadata(new HashMap<>());
                OffsetDateTime nowOffset = OffsetDateTime.now();
                audioAsset.setCreatedAt(nowOffset);
                audioAsset.setUpdatedAt(nowOffset);
                materialAssetRepository.save(audioAsset);
            }
            questionOrder++;
        }
    }
}
