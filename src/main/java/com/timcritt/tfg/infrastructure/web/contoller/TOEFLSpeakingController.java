package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingNavigationUseCaseService;
import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingNavigationUseCaseService.MaterialNodeWithAssets;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeWithAssetsDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/toefl-speaking")
public class TOEFLSpeakingController {
    private final TOEFLSpeakingNavigationUseCaseService useCase;

    public TOEFLSpeakingController(TOEFLSpeakingNavigationUseCaseService useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/material/{materialId}/part/{partNumber}/question/{questionNumber}")
    public ResponseEntity<MaterialNodeWithAssetsDto> getQuestion(
            @PathVariable Long materialId,
            @PathVariable int partNumber,
            @PathVariable int questionNumber) {
        return useCase.getQuestion(materialId, partNumber, questionNumber)
                .map(MaterialNodeWithAssetsDtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
