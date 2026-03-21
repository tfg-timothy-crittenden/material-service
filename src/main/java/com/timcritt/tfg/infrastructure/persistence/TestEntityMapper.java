package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.TestItem;
import com.timcritt.tfg.infrastructure.persistence.jpa.TestJpaEntity;

// This class provides static methods to convert between the domain model (TestItem) and the JPA entity (TestJpaEntity).

public final class TestEntityMapper {
    private TestEntityMapper() {}

    public static TestItem toDomain(TestJpaEntity e) {
        if (e == null) return null;
        return new TestItem(e.getId(), e.getName());
    }

    public static TestJpaEntity toEntity(TestItem d) {
        if (d == null) return null;
        TestJpaEntity e = new TestJpaEntity();
        e.setId(d.getId());
        e.setName(d.getName());
        return e;
    }
}

