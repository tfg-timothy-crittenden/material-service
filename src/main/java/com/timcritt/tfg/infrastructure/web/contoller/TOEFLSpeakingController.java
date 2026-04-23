package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingMaterialCommandService;
import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingNavigationUseCaseService;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingPart1UploadDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeWithAssetsDtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/toefl-speaking")
public class TOEFLSpeakingController {
    private final TOEFLSpeakingNavigationUseCaseService useCase;
    private final TOEFLSpeakingMaterialCommandService commandService;

    public TOEFLSpeakingController(TOEFLSpeakingNavigationUseCaseService useCase, TOEFLSpeakingMaterialCommandService commandService) {
        this.useCase = useCase;
        this.commandService = commandService;
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

    @PostMapping(value = "/material/part1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpeakingPart1(@Valid @ModelAttribute TOEFLSpeakingPart1UploadDto dto, HttpServletRequest request) {
        // Strict field validation for multipart: reject extra fields
        Set<String> allowedFields = new HashSet<>(Arrays.asList(
                "materialTitle", "materialDescription", "materialId", "partImage", "partTitle"
        ));
        // Allow questions[n].transcriptText and questions[n].audio
        int questionCount = dto.getQuestions() != null ? dto.getQuestions().size() : 0;
        for (int i = 0; i < questionCount; i++) {
            allowedFields.add("questions[" + i + "].transcriptText");
            allowedFields.add("questions[" + i + "].audio");
            allowedFields.add("questions[" + i + "].config"); // Allow config field
        }
        // Collect all parameter names from the request
        Set<String> actualFields = new HashSet<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            actualFields.add(paramNames.nextElement());
        }
        // Also check file parts
        if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
            for (String partName : multipartRequest.getFileMap().keySet()) {
                actualFields.add(partName);
            }
        }
        // Find unexpected fields
        Set<String> unexpected = new HashSet<>(actualFields);
        unexpected.removeAll(allowedFields);
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Unexpected fields in request: " + unexpected);
        }
        commandService.uploadSpeakingPart1(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sections-summaries")
    public ResponseEntity<List<SpeakingSectionSummaryDto>> getAllSpeakingSectionSummaries() {
        List<SpeakingSectionSummaryDto> sections = useCase.getAllSpeakingSectionSummaries();
        return ResponseEntity.ok(sections);
    }
}
