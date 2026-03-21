package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.TestItem;
import com.timcritt.tfg.infrastructure.web.dto.TestDto;

public final class TestDtoMapper {
    private TestDtoMapper() {}

    public static TestDto toDto(TestItem d) {
        if (d == null) return null;
        TestDto dto = new TestDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        return dto;
    }

    public static TestItem toDomain(TestDto dto) {
        if (dto == null) return null;
        return new TestItem(dto.getId(), dto.getName());
    }
}

