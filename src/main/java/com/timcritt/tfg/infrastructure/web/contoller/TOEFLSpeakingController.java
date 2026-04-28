package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.infrastructure.security.authorization.MaterialId;
import com.timcritt.tfg.infrastructure.security.authorization.RequireMaterialReadAccess;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionEditDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingPart1UploadDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUpdateDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUploadDto;
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

    @PostMapping(value = "/material/part1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpeakingPart1(@Valid @ModelAttribute TOEFLSpeakingPart1UploadDto dto, HttpServletRequest request) {
        multipartFieldValidator.validatePart1Upload(request, dto);

        commandUseCase.uploadSpeakingPart1(TOEFLSpeakingUploadCommandMapper.toPart1Command(dto));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/material/section/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpeakingSection(@Valid @ModelAttribute TOEFLSpeakingSectionUploadDto dto, HttpServletRequest request) {
        multipartFieldValidator.validateSectionUpload(request, dto);

        commandUseCase.uploadSpeakingSection(TOEFLSpeakingUploadCommandMapper.toSectionCommand(dto));
        return ResponseEntity.ok().build();
    }

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


    @GetMapping("/material/{materialId}/section")
    public ResponseEntity<SpeakingSectionEditDto> getSpeakingSectionForEdit(@MaterialId @PathVariable Long materialId) {
        return navigationUseCase.getSpeakingSectionForEdit(materialId)
                .map(SpeakingSectionEditDtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sections-summaries")
    public ResponseEntity<List<SpeakingSectionSummaryDto>> getAllSpeakingSectionSummaries() {
        List<SpeakingSectionSummaryDto> sections = navigationUseCase.getAllSpeakingSectionSummaries()
                .stream()
                .map(SpeakingSectionSummaryDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(sections);
    }
}
