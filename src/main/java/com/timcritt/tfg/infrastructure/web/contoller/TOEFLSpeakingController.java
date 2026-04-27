package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingPart1UploadDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionEditDto;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUploadDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUpdateDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeWithAssetsDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.SpeakingSectionEditDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.SpeakingSectionSummaryDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.TOEFLSpeakingUploadCommandMapper;
import com.timcritt.tfg.infrastructure.security.authorization.MaterialId;
import com.timcritt.tfg.infrastructure.security.authorization.RequireMaterialReadAccess;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/toefl-speaking")
public class TOEFLSpeakingController {

    /**
     * Matches any indexed question field sent in a section-update request,
     * e.g. {@code questions[3].audio} or {@code part2Questions[0].transcriptText}.
     */
    private static final Pattern UPDATE_QUESTION_FIELD_PATTERN =
            Pattern.compile("^(questions|part2Questions)\\[\\d+]\\.(transcriptText|config|audio)$");
    private static final Set<String> UPDATE_STATIC_FIELDS = Set.of(
            "materialTitle", "materialDescription", "partTitle", "partImage", "part2Title");
    private final TOEFLSpeakingNavigationUseCase navigationUseCase;
    private final TOEFLSpeakingMaterialCommandUseCase commandUseCase;

    public TOEFLSpeakingController(
            TOEFLSpeakingNavigationUseCase navigationUseCase,
            TOEFLSpeakingMaterialCommandUseCase commandUseCase) {
        this.navigationUseCase = navigationUseCase;
        this.commandUseCase = commandUseCase;
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
        Set<String> allowedFields = new HashSet<>(Arrays.asList(
                "materialTitle", "materialDescription", "materialId", "partImage", "partTitle"
        ));
        addQuestionFields(allowedFields, "questions", dto.getQuestions() != null ? dto.getQuestions().size() : 0);
        validateUnexpectedMultipartFields(request, allowedFields);

        commandUseCase.uploadSpeakingPart1(TOEFLSpeakingUploadCommandMapper.toPart1Command(dto));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/material/section/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSpeakingSection(@Valid @ModelAttribute TOEFLSpeakingSectionUploadDto dto, HttpServletRequest request) {
        Set<String> allowedFields = new HashSet<>(Arrays.asList(
                "materialTitle", "materialDescription", "materialId", "partImage", "partTitle", "part2Title"
        ));
        addQuestionFields(allowedFields, "questions", dto.getQuestions() != null ? dto.getQuestions().size() : 0);
        addQuestionFields(allowedFields, "part2Questions", dto.getPart2Questions() != null ? dto.getPart2Questions().size() : 0);
        validateUnexpectedMultipartFields(request, allowedFields);

        commandUseCase.uploadSpeakingSection(TOEFLSpeakingUploadCommandMapper.toSectionCommand(dto));
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/material/{materialId}/section", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateSpeakingSection(
            @PathVariable Long materialId,
            @ModelAttribute TOEFLSpeakingSectionUpdateDto dto,
            HttpServletRequest request) {
        validateUpdateMultipartFields(request);
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

    private void addQuestionFields(Set<String> allowedFields, String fieldPrefix, int questionCount) {
        for (int i = 0; i < questionCount; i++) {
            allowedFields.add(fieldPrefix + "[" + i + "].transcriptText");
            allowedFields.add(fieldPrefix + "[" + i + "].audio");
            allowedFields.add(fieldPrefix + "[" + i + "].config");
        }
    }

    private void validateUnexpectedMultipartFields(HttpServletRequest request, Set<String> allowedFields) {
        Set<String> actualFields = new HashSet<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            actualFields.add(paramNames.nextElement());
        }

        if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
            actualFields.addAll(multipartRequest.getFileMap().keySet());
        }

        Set<String> unexpected = new HashSet<>(actualFields);
        unexpected.removeAll(allowedFields);
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Unexpected fields in request: " + unexpected);
        }
    }

    /**
     * Validates that every field in a section-update multipart request is either a known
     * static field or matches the indexed question-field pattern.
     */
    private void validateUpdateMultipartFields(HttpServletRequest request) {
        Set<String> actualFields = new HashSet<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            actualFields.add(paramNames.nextElement());
        }
        if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest mpr) {
            actualFields.addAll(mpr.getFileMap().keySet());
        }

        Set<String> unexpected = actualFields.stream()
                .filter(f -> !UPDATE_STATIC_FIELDS.contains(f)
                          && !UPDATE_QUESTION_FIELD_PATTERN.matcher(f).matches())
                .collect(Collectors.toSet());
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Unexpected fields in update request: " + unexpected);
        }
    }
}
