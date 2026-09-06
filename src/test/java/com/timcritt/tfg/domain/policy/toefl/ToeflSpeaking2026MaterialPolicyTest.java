package com.timcritt.tfg.domain.policy.toefl;

import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToeflSpeaking2026MaterialPolicyTest {

    private final ToeflSpeaking2026MaterialPolicy policy = new ToeflSpeaking2026MaterialPolicy();

    @Test
    void validateForPublication_acceptsCompleteSpeakingSection() {
        Material material = validMaterial();

        assertThatCode(() -> policy.validateForPublication(material))
                .doesNotThrowAnyException();
    }

    @Test
    void validateForPublication_rejectsMissingMaterial() {
        assertThatThrownBy(() -> policy.validateForPublication(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("material is required");
    }

    @Test
    void validateForPublication_rejectsMissingTitle() {
        Material material = Material.builder()
                .id(10001L)
                .title("   ")
                .status(null)
                .build();
        material.attachRoot(validMaterial().getRoot());

        assertThatThrownBy(() -> policy.validateForPublication(material))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot publish: material title is required");
    }

    @Test
    void validateForPublication_rejectsMissingRoot() {
        Material material = Material.builder()
                .id(10001L)
                .title("TOEFL Speaking Test 1")
                .status(null)
                .build();

        assertThatThrownBy(() -> policy.validateForPublication(material))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot publish: material root is missing");
    }

    @Test
    void validateForPublication_rejectsMissingPart1() {
        Long materialId = 10001L;
        Material material = Material.builder()
                .id(materialId)
                .title("TOEFL Speaking Test 1")
                .build();

        MaterialNode root = MaterialNode.builder()
                .id(20100L)
                .materialId(materialId)
                .parentNodeId(null)
                .kind("SECTION")
                .title("TOEFL Speaking Test 1")
                .displayOrder(0)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(Instant.parse("2026-09-06T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-06T10:00:00Z"))
                .build();

        MaterialNode part2 = partNode(20102L, materialId, 20100L, 1, "Part 2");
        root.addChild(part2);
        material.attachRoot(root);

        assertThatThrownBy(() -> policy.validateForPublication(material))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot publish: Part 1 is missing");
    }

    @Test
    void validateForPublication_rejectsMissingPart2QuestionAudio() {
        Material material = validMaterial();
        MaterialNode part2 = material.getRoot().getChildren().get(1);
        MaterialNode question = part2.getChildren().get(0);
        MaterialNode questionWithoutAudio = questionNode(
                question.getId(),
                question.getMaterialId(),
                question.getParentNodeId(),
                question.getDisplayOrder(),
                question.getTranscriptText()
        );
        part2 = partNode(20102L, material.getId(), 20100L, 1, "Part 2");
        part2.addChild(questionWithoutAudio);
        for (int i = 1; i < 4; i++) {
            MaterialNode q = questionNode(20120L + i, material.getId(), 20102L, i, "P2 Q" + (i + 1));
            q.addAsset(audioAsset(q.getId()));
            part2.addChild(q);
        }
        MaterialNode root = material.getRoot();
        MaterialNode part1 = root.getChildren().get(0);
        root = MaterialNode.builder()
                .id(root.getId())
                .materialId(material.getId())
                .parentNodeId(null)
                .kind(root.getKind())
                .title(root.getTitle())
                .displayOrder(root.getDisplayOrder())
                .responseMode(root.getResponseMode())
                .responseRequired(root.getResponseRequired())
                .scoringMode(root.getScoringMode())
                .config(new HashMap<>())
                .version(root.getVersion())
                .createdAt(root.getCreatedAt())
                .updatedAt(root.getUpdatedAt())
                .build();
        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);

        assertThatThrownBy(() -> policy.validateForPublication(material))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot publish: Part 2 question 0 is missing audio");
    }

    private static Material validMaterial() {
        Long materialId = 10001L;

        Material material = Material.builder()
                .id(materialId)
                .title("TOEFL Speaking Test 1")
                .status(null)
                .build();

        MaterialNode root = MaterialNode.builder()
                .id(20100L)
                .materialId(materialId)
                .parentNodeId(null)
                .kind("SECTION")
                .title("TOEFL Speaking Test 1")
                .displayOrder(0)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(Instant.parse("2026-09-06T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-06T10:00:00Z"))
                .build();

        MaterialNode part1 = partNode(20101L, materialId, 20100L, 0, "Part 1");
        part1.addAsset(imageAsset(20101L));
        for (int i = 0; i < 7; i++) {
            MaterialNode question = questionNode(20110L + i, materialId, 20101L, i, "P1 Q" + (i + 1));
            question.addAsset(audioAsset(question.getId()));
            part1.addChild(question);
        }

        MaterialNode part2 = partNode(20102L, materialId, 20100L, 1, "Part 2");
        for (int i = 0; i < 4; i++) {
            MaterialNode question = questionNode(20120L + i, materialId, 20102L, i, "P2 Q" + (i + 1));
            question.addAsset(audioAsset(question.getId()));
            part2.addChild(question);
        }

        root.addChild(part1);
        root.addChild(part2);
        material.attachRoot(root);
        return material;
    }

    private static MaterialNode partNode(Long id, Long materialId, Long parentNodeId, int displayOrder, String title) {
        return MaterialNode.builder()
                .id(id)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind("PART")
                .title(title)
                .displayOrder(displayOrder)
                .responseMode("NONE")
                .responseRequired(false)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(Instant.parse("2026-09-06T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-06T10:00:00Z"))
                .build();
    }

    private static MaterialNode questionNode(Long id, Long materialId, Long parentNodeId, int displayOrder, String transcriptText) {
        return MaterialNode.builder()
                .id(id)
                .materialId(materialId)
                .parentNodeId(parentNodeId)
                .kind("ITEM")
                .title("Question " + (displayOrder + 1))
                .displayOrder(displayOrder)
                .transcriptText(transcriptText)
                .responseMode("SPOKEN")
                .responseRequired(true)
                .scoringMode("NONE")
                .config(new HashMap<>())
                .version(0L)
                .createdAt(Instant.parse("2026-09-06T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-06T10:00:00Z"))
                .build();
    }

    private static MaterialAsset imageAsset(Long nodeId) {
        MaterialAsset asset = new MaterialAsset();
        asset.setId(nodeId + 1000);
        asset.setMaterialNodeId(nodeId);
        asset.setKind(MaterialAsset.Kind.IMAGE);
        asset.setStorageKey("speaking/10001/part1/image/image.png");
        asset.setDisplayOrder(0);
        return asset;
    }

    private static MaterialAsset audioAsset(Long nodeId) {
        MaterialAsset asset = new MaterialAsset();
        asset.setId(nodeId + 2000);
        asset.setMaterialNodeId(nodeId);
        asset.setKind(MaterialAsset.Kind.AUDIO);
        asset.setStorageKey("speaking/10001/part/audio/question.mp3");
        asset.setDisplayOrder(0);
        return asset;
    }
}
