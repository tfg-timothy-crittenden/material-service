package com.timcritt.tfg.domain.policy.toefl;

import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.policy.MaterialPolicy;

import java.util.Comparator;
import java.util.List;

public final class ToeflSpeaking2026MaterialPolicy implements MaterialPolicy {

    private static final int PART_1_QUESTION_COUNT = 7;
    private static final int PART_2_QUESTION_COUNT = 4;

    @Override
    public void validateForPublication(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("material is required");
        }

        if (!hasText(material.getTitle())
                || "Untitled Draft".equals(material.getTitle())) {
            throw new IllegalStateException(
                    "Cannot publish: material title is required"
            );
        }

        MaterialNode root = material.getRoot();

        if (root == null) {
            throw new IllegalStateException(
                    "Cannot publish: material root is missing"
            );
        }

        List<MaterialNode> parts = root.getChildren().stream()
                .filter(node -> "PART".equals(node.getKind()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();

        MaterialNode part1 = findPart(parts, 0, "Part 1");
        MaterialNode part2 = findPart(parts, 1, "Part 2");

        validatePart1(part1);
        validatePart2(part2);
    }

    private void validatePart1(MaterialNode part1) {
        if (!hasText(part1.getTitle())) {
            throw new IllegalStateException(
                    "Cannot publish: Part 1 title is required"
            );
        }

        boolean hasImage = part1.getAssets().stream()
                .anyMatch(asset -> asset.getKind() == MaterialAsset.Kind.IMAGE);

        if (!hasImage) {
            throw new IllegalStateException(
                    "Cannot publish: Part 1 image is required"
            );
        }

        List<MaterialNode> questions = questions(part1);

        if (questions.size() != PART_1_QUESTION_COUNT) {
            throw new IllegalStateException(
                    "Cannot publish: Part 1 must have exactly "
                            + PART_1_QUESTION_COUNT
                            + " questions (found "
                            + questions.size()
                            + ")"
            );
        }

        validateQuestions(questions, "Part 1");
    }

    private void validatePart2(MaterialNode part2) {
        if (!hasText(part2.getTitle())) {
            throw new IllegalStateException(
                    "Cannot publish: Part 2 title is required"
            );
        }

        List<MaterialNode> questions = questions(part2);

        if (questions.size() != PART_2_QUESTION_COUNT) {
            throw new IllegalStateException(
                    "Cannot publish: Part 2 must have exactly "
                            + PART_2_QUESTION_COUNT
                            + " questions (found "
                            + questions.size()
                            + ")"
            );
        }

        validateQuestions(questions, "Part 2");
    }

    private void validateQuestions(
            List<MaterialNode> questions,
            String partName
    ) {
        for (MaterialNode question : questions) {

            if (!hasText(question.getTranscriptText())) {
                throw new IllegalStateException(
                        "Cannot publish: all "
                                + partName
                                + " questions must have transcript text"
                );
            }

            boolean hasAudio = question.getAssets().stream()
                    .anyMatch(asset ->
                            asset.getKind() == MaterialAsset.Kind.AUDIO
                    );

            if (!hasAudio) {
                throw new IllegalStateException(
                        "Cannot publish: "
                                + partName
                                + " question "
                                + question.getDisplayOrder()
                                + " is missing audio"
                );
            }
        }
    }

    private List<MaterialNode> questions(MaterialNode part) {
        return part.getChildren().stream()
                .filter(node -> "ITEM".equals(node.getKind()))
                .sorted(Comparator.comparing(MaterialNode::getDisplayOrder))
                .toList();
    }

    private MaterialNode findPart(
            List<MaterialNode> parts,
            int displayOrder,
            String name
    ) {
        return parts.stream()
                .filter(node ->
                        Integer.valueOf(displayOrder)
                                .equals(node.getDisplayOrder())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Cannot publish: " + name + " is missing"
                        )
                );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}