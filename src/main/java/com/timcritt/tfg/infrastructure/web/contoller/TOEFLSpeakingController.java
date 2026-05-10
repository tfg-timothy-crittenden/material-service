package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.infrastructure.security.authorization.MaterialId;
import com.timcritt.tfg.infrastructure.security.authorization.RequireMaterialReadAccess;
import com.timcritt.tfg.infrastructure.web.dto.DraftSaveResponseDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialAssetDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionEditDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUpdateDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUploadDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialAssetDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeWithAssetsDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.SpeakingSectionEditDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.SpeakingSectionSummaryDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.TOEFLSpeakingUploadCommandMapper;
import com.timcritt.tfg.infrastructure.web.validation.SpeakingMultipartFieldValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/toefl-speaking")
public class TOEFLSpeakingController {

    private final TOEFLSpeakingNavigationUseCase navigationUseCase;
    private final TOEFLSpeakingMaterialCommandUseCase commandUseCase;
    private final SpeakingMultipartFieldValidator multipartFieldValidator;

    public TOEFLSpeakingController(
            TOEFLSpeakingNavigationUseCase navigationUseCase,
            TOEFLSpeakingMaterialCommandUseCase commandUseCase,
            SpeakingMultipartFieldValidator multipartFieldValidator) {
        this.navigationUseCase = navigationUseCase;
        this.commandUseCase = commandUseCase;
        this.multipartFieldValidator = multipartFieldValidator;
    }

    //Only accessible to users that have been assigned the material
    @RequireMaterialReadAccess
    @GetMapping("/material/{materialId}/part/{partNumber}/question/{questionNumber}")
    public ResponseEntity<MaterialNodeWithAssetsDto> getQuestion(
            @MaterialId @PathVariable Long materialId,
            @PathVariable int partNumber,
            @PathVariable int questionNumber) {
        return navigationUseCase.getQuestion(materialId, partNumber, questionNumber)
                .map(MaterialNodeWithAssetsDtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    //Should only be available to role: content_author
    @PostMapping(value = "/material/section/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpeakingSection(@Valid @ModelAttribute TOEFLSpeakingSectionUploadDto dto, HttpServletRequest request) {
        multipartFieldValidator.validateSectionUpload(request, dto);

        commandUseCase.uploadSpeakingSection(TOEFLSpeakingUploadCommandMapper.toSectionCommand(dto));
        return ResponseEntity.ok().build();
    }

    //Should only be available to role: content_author
    // Drafts intentionally bypass bean validation so incomplete multipart payloads can be saved.
    @PostMapping(value = "/material/section/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DraftSaveResponseDto> saveSpeakingSectionDraft(@ModelAttribute TOEFLSpeakingSectionUploadDto dto, HttpServletRequest request) {
        multipartFieldValidator.validateSectionUpload(request, dto);

        Long materialId = commandUseCase.uploadSpeakingSection(TOEFLSpeakingUploadCommandMapper.toSectionCommand(dto));
        return ResponseEntity.ok(new DraftSaveResponseDto(materialId));
    }

    //Should only be available to role: content_author
    @PatchMapping(value = "/material/{materialId}/section", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateSpeakingSection(
            @PathVariable Long materialId,
            @ModelAttribute TOEFLSpeakingSectionUpdateDto dto,
            HttpServletRequest request) {
        multipartFieldValidator.validateSectionPatch(request);
        commandUseCase.updateSpeakingSection(
                TOEFLSpeakingUploadCommandMapper.toSectionUpdateCommand(materialId, dto));
        return ResponseEntity.ok().build();
    }


    //Should only be available to roles: admin, content_author
    @GetMapping("/material/{materialId}/section")
    public ResponseEntity<SpeakingSectionEditDto> getSpeakingSectionForEdit(@MaterialId @PathVariable Long materialId) {
        return navigationUseCase.getSpeakingSectionForEdit(materialId)
                .map(SpeakingSectionEditDtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Should only be accessible to roles: admin, content_author
    @GetMapping("/sections-summaries")
    public ResponseEntity<List<SpeakingSectionSummaryDto>> getAllSpeakingSectionSummaries() {
        List<SpeakingSectionSummaryDto> sections = navigationUseCase.getAllSpeakingSectionSummaries()
                .stream()
                .map(SpeakingSectionSummaryDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(sections);
    }

    //Should only be accessible to roles: admin, content_author
    @GetMapping("/sections-summaries/drafts")
    public ResponseEntity<List<SpeakingSectionSummaryDto>> getDraftSpeakingSectionSummaries() {
        List<SpeakingSectionSummaryDto> sections = navigationUseCase.getDraftSpeakingSectionSummaries()
                .stream()
                .map(SpeakingSectionSummaryDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(sections);
    }

    // Should only be available to role: content_author
    // Validates completeness then flips status from DRAFT → PUBLISHED.
    @PatchMapping("/material/{materialId}/publish")
    public ResponseEntity<Void> publishSpeakingSection(@PathVariable Long materialId) {
        commandUseCase.publishSpeakingSection(materialId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/material/{materialId}")
    public ResponseEntity<Void> deleteSpeakingSection(@PathVariable Long materialId) {
        commandUseCase.deleteSpeakingSection(materialId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/material-nodes/{nodeId}/assets")
    public ResponseEntity<List<MaterialAssetDto>> getAssetsByMaterialNodeId(@PathVariable Long nodeId) {
        List<MaterialAssetDto> dtos = navigationUseCase.getAssetsByMaterialNodeId(nodeId)
                .stream()
                .map(MaterialAssetDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
