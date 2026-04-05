package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;

import java.util.List;

public interface MaterialAggregationUseCase {

    List<MaterialNodeTreeDto> getMaterialNodeTreeBySectionId(Long sectionId);
}

