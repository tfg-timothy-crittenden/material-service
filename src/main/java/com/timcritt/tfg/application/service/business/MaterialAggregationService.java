package com.timcritt.tfg.application.service.business;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaterialAggregationService implements MaterialAggregationUseCase {
    private final MaterialNodeRepositoryPort materialNodeRepository;

    public MaterialAggregationService(MaterialNodeRepositoryPort materialNodeRepositoryPort) {
        this.materialNodeRepository = materialNodeRepositoryPort;
    }

    @Override
    public List<MaterialNodeTreeDto> getMaterialNodeTreeBySectionId(Long sectionId) {
        // Fetch all descendants (including the root) using a recursive query via the repository
        List<MaterialNode> allNodes = materialNodeRepository.findAllDescendantsByRootId(sectionId);
        System.out.println(allNodes);
        // Map to DTOs and build the tree
        return buildTreeFromFlatList(allNodes, sectionId);
    }

    /**
     * Builds a tree of MaterialNodeTreeDto from a flat list of MaterialNode, preserving parent-child relationships.
     */
    private List<MaterialNodeTreeDto> buildTreeFromFlatList(List<MaterialNode> nodes, Long rootId) {
        // Map node id to DTO
        Map<Long, MaterialNodeTreeDto> dtoMap = nodes.stream()
                .collect(Collectors.toMap(MaterialNode::getId, MaterialNodeTreeDto::fromDomain));
        // Build tree
        List<MaterialNodeTreeDto> roots = new ArrayList<>();
        for (MaterialNode node : nodes) {
            MaterialNodeTreeDto dto = dtoMap.get(node.getId());
            Long parentId = node.getParentNodeId();
            if (parentId == null || parentId.equals(rootId)) {
                roots.add(dto);
            } else {
                MaterialNodeTreeDto parentDto = dtoMap.get(parentId);
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

}
