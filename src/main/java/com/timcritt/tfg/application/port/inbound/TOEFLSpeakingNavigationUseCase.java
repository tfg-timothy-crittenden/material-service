package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.application.dto.MaterialNodeWithAssetsResult;
import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.dto.SpeakingSectionSummary;
import com.timcritt.tfg.domain.model.MaterialAsset;

import java.util.List;
import java.util.Optional;

public interface TOEFLSpeakingNavigationUseCase {
    Optional<MaterialNodeWithAssetsResult> getQuestion(Long materialId, int partOrder, int questionOrder);
    Optional<SpeakingSectionEditResult> getSpeakingSectionForEdit(Long materialId);
    List<SpeakingSectionSummary> getAllSpeakingSectionSummaries();
    List<SpeakingSectionSummary> getDraftSpeakingSectionSummaries();
    List<MaterialAsset> getAssetsByMaterialNodeId(Long nodeId);
}

