package com.timcritt.tfg.infrastructure.web.validation;

import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUploadDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SpeakingMultipartFieldValidator {

    private static final Pattern UPDATE_QUESTION_FIELD_PATTERN =
            Pattern.compile("^(questions|part2Questions)\\[\\d+]\\.(transcriptText|config|audio|removeAudio)$");

    private static final Set<String> UPDATE_STATIC_FIELDS = Set.of(
            "materialId", "materialTitle", "materialDescription", "partTitle", "partImage", "removePartImage", "part2Title");


    public void validateSectionUpload(HttpServletRequest request, TOEFLSpeakingSectionUploadDto dto) {
        Set<String> allowedFields = new HashSet<>(Arrays.asList(
                "materialTitle", "materialDescription", "materialId", "partImage", "partTitle", "part2Title"
        ));
        addQuestionFields(allowedFields, "questions", dto.getQuestions() != null ? dto.getQuestions().size() : 0);
        addQuestionFields(allowedFields, "part2Questions", dto.getPart2Questions() != null ? dto.getPart2Questions().size() : 0);
        validateUnexpectedMultipartFields(request, allowedFields);
    }

    public void validateSectionPatch(HttpServletRequest request) {
        Set<String> actualFields = getActualFields(request);
        Set<String> unexpected = actualFields.stream()
                .filter(f -> !UPDATE_STATIC_FIELDS.contains(f)
                        && !UPDATE_QUESTION_FIELD_PATTERN.matcher(f).matches())
                .collect(Collectors.toSet());
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Unexpected fields in update request: " + unexpected);
        }
    }

    private void addQuestionFields(Set<String> allowedFields, String fieldPrefix, int questionCount) {
        for (int i = 0; i < questionCount; i++) {
            allowedFields.add(fieldPrefix + "[" + i + "].transcriptText");
            allowedFields.add(fieldPrefix + "[" + i + "].audio");
            allowedFields.add(fieldPrefix + "[" + i + "].config");
        }
    }

    private void validateUnexpectedMultipartFields(HttpServletRequest request,
                                                   Set<String> allowedFields) {
        Set<String> actualFields = getActualFields(request);
        Set<String> unexpected = new HashSet<>(actualFields);
        unexpected.removeAll(allowedFields);
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Unexpected fields in request: " + unexpected);
        }
    }

    private Set<String> getActualFields(HttpServletRequest request) {
        Set<String> actualFields = new HashSet<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            actualFields.add(paramNames.nextElement());
        }

        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            actualFields.addAll(multipartRequest.getFileMap().keySet());
        }

        return actualFields;
    }
}
