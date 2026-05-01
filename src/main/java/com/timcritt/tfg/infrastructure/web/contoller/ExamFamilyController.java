package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.ExamFamilyUseCase;
import com.timcritt.tfg.infrastructure.web.dtoMapper.ExamFamilyDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.ExamFamilyDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST controller for the exam-family API.
// Uses ExamFamilyDto as the transport model and ExamFamilyDtoMapper to convert to/from the domain model.

@RestController
@RequestMapping("/api/tests")
public class ExamFamilyController {

    private final ExamFamilyUseCase useCase;

    public ExamFamilyController(ExamFamilyUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamFamilyDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ExamFamilyDtoMapper.toDto(useCase.getTestById(id)));
    }
}
