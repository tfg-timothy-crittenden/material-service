package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.port.inbound.TestUseCase;
import com.timcritt.tfg.infrastructure.web.dto.TestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// This is a simple REST controller that exposes the TestUseCase as an API.
// It uses TestDto as the data transfer object for requests and responses, and TestDtoMapper to convert between the domain model and the DTO.

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestUseCase useCase;

    public TestController(TestUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/{name}")
    public ResponseEntity<TestDto> create(@PathVariable String name) {
        return ResponseEntity.ok(TestDtoMapper.toDto(useCase.createTest(name)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TestDtoMapper.toDto(useCase.getTestById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestDto> update(@PathVariable Long id, @RequestBody TestDto dto) {
        return ResponseEntity.ok(TestDtoMapper.toDto(useCase.updateTest(id, dto.getName())));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteTest(id);
        return ResponseEntity.noContent().build();
    }


}
